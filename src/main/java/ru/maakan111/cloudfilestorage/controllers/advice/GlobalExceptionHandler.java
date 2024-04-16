package ru.maakan111.cloudfilestorage.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.maakan111.cloudfilestorage.utils.PathUtils;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String MAX_UPLOAD_SIZE;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadException(HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {
        LOGGER.warn("Max upload size exceeded: {}", MAX_UPLOAD_SIZE);
        redirectAttributes.addFlashAttribute("failureAlert",
                "Uploaded object size must not exceed " + MAX_UPLOAD_SIZE);
        String path = request.getParameter("path");
        return "redirect:/" + PathUtils.getPathParam(path);
    }

    @ExceptionHandler(Throwable.class)
    public String logAllUnhandledExceptions(Exception exception) {
        LOGGER.error("An error occurred", exception);
        return "redirect:/error";
    }
}