package ru.maakan111.cloudfilestorage.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.maakan111.cloudfilestorage.dto.FilesUploadDto;
import ru.maakan111.cloudfilestorage.dto.ObjectMoveDto;
import ru.maakan111.cloudfilestorage.dto.ObjectRenameDto;
import ru.maakan111.cloudfilestorage.dto.SearchDto;
import ru.maakan111.cloudfilestorage.models.security.UserDetailsImpl;
import ru.maakan111.cloudfilestorage.utils.PathUtils;
import ru.maakan111.cloudfilestorage.exceptions.ObjectAlreadyExistsException;
import ru.maakan111.cloudfilestorage.exceptions.ObjectNotExistsException;
import ru.maakan111.cloudfilestorage.exceptions.ObjectUploadException;
import ru.maakan111.cloudfilestorage.services.FileService;

import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;

    @GetMapping("/download")
    public void downloadFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String path,
            HttpServletResponse response) {
        try (InputStream fileContentStream = fileService.getFileContent(userDetails.getUserId(), path)) {
            response.setHeader("Content-Disposition",
                    String.format("attachment; filename=\"%s\"", PathUtils.extractObjectName(path)));
            FileCopyUtils.copy(fileContentStream, response.getOutputStream());
        } catch (ObjectNotExistsException | IOException exc) {
            LOGGER.warn("Failed to download file \"{}\":", path, exc);
        }
    }

    @PostMapping("/upload")
    public String uploadFiles(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String path,
            @ModelAttribute("filesUploadDto") @Valid FilesUploadDto filesUploadDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("failureAlert", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/" + PathUtils.getPathParam(path);
        }
        try {
            fileService.saveFiles(userDetails.getUserId(), filesUploadDto);
        } catch (ObjectAlreadyExistsException exc) {
            LOGGER.debug("Failed to upload files", exc);
            redirectAttributes.addFlashAttribute("failureAlert", "File with this name already exists");
        } catch (ObjectUploadException exc) {
            LOGGER.warn("Failed to upload files", exc);
            redirectAttributes.addFlashAttribute("failureAlert", "An error occurred during uploading");
        }
        return "redirect:/" + PathUtils.getPathParam(path);
    }

    @GetMapping("/rename")
    public String getFileRenameForm(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String path,
            Model model,
            HttpServletRequest request) {
        try {
            fileService.validateFileExists(userDetails.getUserId(), path);
        } catch (ObjectNotExistsException exc) {
            LOGGER.warn("Failed to get file renaming form for path \"{}\":", path, exc);
            return "redirect:/";
        }

        if (!model.containsAttribute("objectRenameDto")) {
            ObjectRenameDto renameDto = new ObjectRenameDto();
            renameDto.setNewName(PathUtils.extractObjectName(path));
            model.addAttribute("objectRenameDto", renameDto);
        }
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("breadcrumbs", PathUtils.assembleBreadcrumbsFromPath(path));
        model.addAttribute("searchDto", new SearchDto());
        return "rename";
    }

    @PatchMapping("/rename")
    public String renameFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String path,
            @ModelAttribute("objectRenameDto") @Valid ObjectRenameDto objectRenameDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.objectRenameDto", bindingResult);
            redirectAttributes.addFlashAttribute("objectRenameDto", objectRenameDto);
            return "redirect:/file/rename" + PathUtils.getPathParam(path);
        }

        try {
            String parentFolderPath = fileService.renameFile(userDetails.getUserId(), objectRenameDto);
            return "redirect:/" + PathUtils.getPathParam(parentFolderPath);
        } catch (ObjectAlreadyExistsException exc) {
            LOGGER.debug("Failed to rename file", exc);
            redirectAttributes.addFlashAttribute("failureAlert", "File with this name already exists");
            redirectAttributes.addFlashAttribute("objectRenameDto", objectRenameDto);
            return "redirect:/file/rename" + PathUtils.getPathParam(path);
        }
    }

    @GetMapping("/move")
    public String getFileMoveForm(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String path,
            @ModelAttribute("objectMoveDto") ObjectMoveDto objectMoveDto,
            Model model,
            HttpServletRequest request) {
        try {
            model.addAttribute("moveCandidates", fileService.getMoveCandidatesForFile(userDetails.getUserId(), path));
        } catch (ObjectNotExistsException exc) {
            LOGGER.warn("Failed to get file moving form for path \"{}\":", path, exc);
            return "redirect:/";
        }
        model.addAttribute("requestURI", request.getRequestURI());
        model.addAttribute("breadcrumbs", PathUtils.assembleBreadcrumbsFromPath(path));
        model.addAttribute("searchDto", new SearchDto());
        return "move";
    }

    @PutMapping("/move")
    public String moveFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String path,
            @ModelAttribute("objectMoveDto") ObjectMoveDto objectMoveDto,
            RedirectAttributes redirectAttributes) {
        try {
            String oldParentPath = fileService.moveFile(userDetails.getUserId(), objectMoveDto);
            redirectAttributes.addFlashAttribute("successAlert", "File was moved successfully");
            return "redirect:/" + PathUtils.getPathParam(oldParentPath);
        } catch (ObjectAlreadyExistsException exc) {
            LOGGER.debug("Failed to move file", exc);
            redirectAttributes.addFlashAttribute("failureAlert",
                    "File with this name already exists in target location");
        }
        return "redirect:/file/move" + PathUtils.getPathParam(path);
    }

    @DeleteMapping
    public String deleteFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String path) {
        String parentFolder = fileService.deleteFile(userDetails.getUserId(), path);
        return "redirect:/" + PathUtils.getPathParam(parentFolder);
    }
}
