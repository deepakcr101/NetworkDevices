package com.deepak.NetworkDevices;

import com.deepak.NetworkDevices.controller.ShelfController;
import com.deepak.NetworkDevices.dto.request.CreateShelfRequest;
import com.deepak.NetworkDevices.dto.request.UpdateShelfRequest;
import com.deepak.NetworkDevices.dto.response.ShelfDto;
import com.deepak.NetworkDevices.dto.response.ShelfLiteDto;
import com.deepak.NetworkDevices.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShelfController.class)
public class ShelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelfService shelfService;

    @Test
    void getShelvesTest() throws Exception {
        // Arrange
        List<ShelfDto> shelves = List.of(
                new ShelfDto("uuid1", "shelf1", "part1", "device1", "shelfPosition1", "device1-shelfPosition1", null, null),
                new ShelfDto("uuid2", "shelf2", "part2", "device2", "shelfPosition2", "device2-shelfPosition2", null, null)
        );

        when(shelfService.listShelvesWithStatus()).thenReturn(shelves);

        // Act & Assert
        mockMvc.perform(get("/api/v1/shelves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(shelves.size()))
                .andExpect(jsonPath("$[0].shelfId").value(shelves.get(0).shelfId()))
                .andExpect(jsonPath("$[1].shelfName").value("shelf2"));

        verify(shelfService, times(1)).listShelvesWithStatus();
    }

    @Test
    void getShelves_ReturnsEmptyList_WhenNoShelvesExist() throws Exception {
        // Arrange
        when(shelfService.listShelvesWithStatus()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/shelves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(shelfService, times(1)).listShelvesWithStatus();
    }

    @Test
    void createShelfTest() throws Exception {
        // Arrange
        CreateShelfRequest createShelfRequest = new CreateShelfRequest("shelf1", "part1");
        ShelfDto shelfDto = new ShelfDto("uuid1", "shelf1", "part1", "device1", "shelfPosition1", "device1-shelfPosition1", null, null);

        when(shelfService.createShelf(any(CreateShelfRequest.class))).thenReturn("uuid1");
        when(shelfService.getShelf("uuid1")).thenReturn(shelfDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/shelves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "shelfName": "shelf1",
                                    "partName": "part1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shelfId").value("uuid1"))
                .andExpect(jsonPath("$.shelfName").value("shelf1"))
                .andExpect(jsonPath("$.partName").value("part1"));

        verify(shelfService, times(1)).createShelf(any(CreateShelfRequest.class));
        verify(shelfService, times(1)).getShelf("uuid1");
    }

    @Test
    void updateShelfTest() throws Exception {
        // Arrange
        UpdateShelfRequest updateShelfRequest = new UpdateShelfRequest("updatedShelfName", "updatedPartName");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/shelves/uuid1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "shelfName": "updatedShelfName",
                                    "partName": "updatedPartName"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(shelfService, times(1)).updateShelf(eq("uuid1"), any(UpdateShelfRequest.class));
    }

    @Test
    void deleteShelfTest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/shelves/uuid1"))
                .andExpect(status().isNoContent());

        verify(shelfService, times(1)).softDeleteShelf("uuid1");
    }

    @Test
    void listAvailableShelvesTest() throws Exception {
        // Arrange
        List<ShelfLiteDto> availableShelves = List.of(
                new ShelfLiteDto("uuid1", "shelf1", "part1"),
                new ShelfLiteDto("uuid2", "shelf2", "part2")
        );

        when(shelfService.listAvailableShelves()).thenReturn(availableShelves);

        // Act & Assert
        mockMvc.perform(get("/api/v1/shelves/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(availableShelves.size()))
                .andExpect(jsonPath("$[0].shelfId").value("uuid1"))
                .andExpect(jsonPath("$[1].shelfName").value("shelf2"));

        verify(shelfService, times(1)).listAvailableShelves();
    }

    @Test
    void listAvailableShelves_ReturnsEmptyList_WhenNoShelvesExist() throws Exception {
        // Arrange
        when(shelfService.listAvailableShelves()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/shelves/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(shelfService, times(1)).listAvailableShelves();
    }
}