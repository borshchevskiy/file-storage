package ru.borshchevskiy.filestorage.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload2.core.*;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.io.input.BoundedInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.borshchevskiy.filestorage.exception.MultipartProcessingException;
import ru.borshchevskiy.filestorage.exception.NotMultipartRequestException;
import ru.borshchevskiy.filestorage.service.FileService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * Handles files manipulation requests.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/files")
public class FilesController {

    @Value("${app.max-file-size:10485760}")
    private long maxFileSize;
    private final FileService fileService;

    /**
     * Method allows client to download file on specified path.
     *
     * @param path to directory where required file is located.
     * @param file name of the file.
     * @return {@link ResponseEntity} with {@link StreamingResponseBody} representing {@link InputStream}
     * of required file.
     */
    @GetMapping(value = "/download", produces = APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestParam(value = "path") String path,
                                                              @RequestParam(value = "file") String file) {
        String filename = URLEncoder.encode(file, StandardCharsets.UTF_8)
                .replace("+", "%20");
        StreamingResponseBody stream = outputStream ->
                FileCopyUtils.copy(fileService.downloadFile(path, file), outputStream);

        return ResponseEntity
                .status(OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(stream);
    }

    /**
     * Method allows client to upload file to server on specified path.
     * File uploaded using Apache Commons FileUpload 2.
     *
     * @param path               path to directory where file should be uploaded.
     * @param request            {@link HttpServletRequest}.
     * @param redirectAttributes {@link RedirectAttributes}.
     * @return redirect to update viewed file list.
     */
    @PostMapping(value = "/upload")
    public String uploadFile(@RequestParam(value = "path") String path,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        boolean isMultipart = JakartaServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new NotMultipartRequestException("No file found within request!");
        }

        JakartaServletFileUpload upload = new JakartaServletFileUpload<>();
        upload.setFileSizeMax(maxFileSize);
        try {
            FileItemInputIterator itemIterator = upload.getItemIterator(request);
            while (itemIterator.hasNext()) {
                FileItemInput item = itemIterator.next();
                if (!item.isFormField() && StringUtils.hasText(item.getName())) {
                    String name = item.getName();
                    try (InputStream stream = item.getInputStream()) {
                        fileService.uploadFile(stream, path, name);
                        if (stream instanceof BoundedInputStream bis
                                && bis.isPropagateClose()
                                && bis.getCount() >= maxFileSize) {
                            throw new FileUploadByteCountLimitException(
                                    "Error occurred while processing request! File is too large.",
                                    bis.getCount(),
                                    maxFileSize,
                                    name,
                                    item.getFieldName());
                        }
                    }
                }
            }
        } catch (FileUploadByteCountLimitException e) {
            throw new MultipartProcessingException("Error occurred while processing request! File is too large.", e);
        } catch (IOException e) {
            throw new MultipartProcessingException("Error occurred while processing request! Please, try again.", e);
        }
        redirectAttributes.addAttribute("path", path);
        return "redirect:/updateFilesList";
    }

    /**
     * Method handles file renaming requests.
     *
     * @param path               path to the directory where file is located.
     * @param oldName            old name of the file.
     * @param newName            new name of the file.
     * @param redirectAttributes {@link RedirectAttributes}.
     * @return redirect to update viewed file list.
     */
    @PostMapping(value = "/rename")
    public String renameFile(@RequestParam(value = "path") String path,
                             @RequestParam(value = "oldName") String oldName,
                             @RequestParam(value = "newName") String newName,
                             RedirectAttributes redirectAttributes) {

        fileService.renameFile(path, oldName, newName);
        redirectAttributes.addAttribute("path", path);

        return "redirect:/updateFilesList";
    }

    /**
     * Method deletes specified file.
     *
     * @param path               path to the directory where file is located.
     * @param name               name of the file to be deleted.
     * @param redirectAttributes {@link RedirectAttributes}.
     * @return redirect to update viewed file list.
     */
    @PostMapping(value = "/delete")
    public String deleteFile(@RequestParam(value = "path") String path,
                             @RequestParam(value = "name") String name,
                             RedirectAttributes redirectAttributes) {

        fileService.deleteFile(path, name);
        redirectAttributes.addAttribute("path", path);

        return "redirect:/updateFilesList";
    }
}
