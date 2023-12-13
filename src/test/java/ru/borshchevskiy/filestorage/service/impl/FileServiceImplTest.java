package ru.borshchevskiy.filestorage.service.impl;

import io.minio.messages.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.mapper.FileItemMapper;
import ru.borshchevskiy.filestorage.repository.MinioRepository;
import ru.borshchevskiy.filestorage.util.FilePathUtil;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private MinioRepository minioRepository;
    @Mock
    private FileItemMapper fileItemMapper;
    @Mock
    private UserSessionData userSessionData;
    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    @DisplayName("Test getFilesByPath method - " +
            "returns list of fileItemDTOs: directories first, ordered by name, items with empty name omitted")
    public void getFilesByPath() {
        final String path = "testPath";
        final String userFolder = "user-id-files";
        final String fileStoragePath = "user-id-files/testPath";

        final Item item1 = mock(Item.class);
        final Item item2 = mock(Item.class);
        final Item item3 = mock(Item.class);
        final Item item4 = mock(Item.class);
        final Item item5 = mock(Item.class);
        final Item item6 = mock(Item.class);

        final List<Item> itemList = new ArrayList<>();

        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        itemList.add(item6);

        Collections.shuffle(itemList);

        final FileItemDto itemDto1 = new FileItemDto();
        final FileItemDto itemDto2 = new FileItemDto();
        final FileItemDto itemDto3 = new FileItemDto();
        final FileItemDto itemDto4 = new FileItemDto();
        final FileItemDto itemDto5 = new FileItemDto();
        final FileItemDto itemDto6 = new FileItemDto();

        itemDto1.setDirectory(true);
        itemDto2.setDirectory(true);

        itemDto1.setName("item1isDir");
        itemDto2.setName("item2isDir");
        itemDto3.setName("item3");
        itemDto4.setName("item4");
        itemDto5.setName("item5");
        itemDto6.setName("");//empty name - should be omitted in actual list

        final List<FileItemDto> expectedList = new ArrayList<>();

        expectedList.add(itemDto1);
        expectedList.add(itemDto2);
        expectedList.add(itemDto3);
        expectedList.add(itemDto4);
        expectedList.add(itemDto5);

        doReturn(userFolder).when(userSessionData).getUserDirectory();
        try (MockedStatic<FilePathUtil> mockedFilePathUtil = mockStatic(FilePathUtil.class)) {
            mockedFilePathUtil.when(() -> FilePathUtil.addUserDirectoryToPath(userSessionData, path))
                    .thenReturn(fileStoragePath);
        }
        doReturn(itemList).when(minioRepository).getItemsByPath(fileStoragePath, false);
        doReturn(itemDto1).when(fileItemMapper).mapToFileInfoDto(item1);
        doReturn(itemDto2).when(fileItemMapper).mapToFileInfoDto(item2);
        doReturn(itemDto3).when(fileItemMapper).mapToFileInfoDto(item3);
        doReturn(itemDto4).when(fileItemMapper).mapToFileInfoDto(item4);
        doReturn(itemDto5).when(fileItemMapper).mapToFileInfoDto(item5);
        doReturn(itemDto6).when(fileItemMapper).mapToFileInfoDto(item6);

        List<FileItemDto> actualList = fileService.getItemsByPath(path);

        assertThat(actualList).isEqualTo(expectedList);
        assertThat(actualList.get(0).isDirectory()).isTrue();
        assertThat(actualList.get(1).isDirectory()).isTrue();
        assertThat(actualList.get(0).getName()).isEqualTo(itemDto1.getName());
    }

    @Test
    @DisplayName("Test downloadFile method - returns expected inputStream")
    public void downloadFile() {
        final String path = "testPath/";
        final String userFolder = "user-id-files";
        final String fileName = "file.txt";
        final String fileStoragePath = "user-id-files/testPath/";
        final String fileContent = "file content";

        InputStream expectedResult = new ByteArrayInputStream(fileContent.getBytes());

        doReturn(userFolder).when(userSessionData).getUserDirectory();
        try (MockedStatic<FilePathUtil> mockedFilePathUtil = mockStatic(FilePathUtil.class)) {
            mockedFilePathUtil.when(() -> FilePathUtil.addUserDirectoryToPath(userSessionData, path))
                    .thenReturn(fileStoragePath);
        }
        doReturn(expectedResult).when(minioRepository).getFile(fileStoragePath + fileName);

        InputStream actualResult = fileService.downloadFile(path, fileName);

        assertThat(actualResult).isEqualTo(expectedResult);
    }

}