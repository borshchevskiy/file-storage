package ru.borshchevskiy.filestorage.mapper;

import io.minio.messages.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.borshchevskiy.filestorage.dto.file.FileItemDto;
import ru.borshchevskiy.filestorage.web.session.UserSessionData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class FileItemMapperTest {

    @Mock
    UserSessionData userSessionData;
    @Mock
    Item item;
    private final Long BYTES_SIZE = 10240000000L;
    private final String BYTES_SIZE_TEXT = "9,5 GB";


    @Test
    @DisplayName("Test getViewSize method - returns correct human-readable text of file size")
    public void getViewSize() {
        FileItemMapper fileItemMapper = new FileItemMapper(userSessionData);

        doReturn("").when(userSessionData).getUserDirectory();

        doReturn(BYTES_SIZE).when(item).size();
        doReturn(false).when(item).isDir();
        doReturn("item").when(item).objectName();

        FileItemDto fileItemDto = fileItemMapper.mapToFileItemDto(item);

        assertEquals(BYTES_SIZE_TEXT, fileItemDto.getViewSize());
    }
}