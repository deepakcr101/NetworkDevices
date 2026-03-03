package com.deepak.NetworkDevices;

import com.deepak.NetworkDevices.controller.DeviceController;
import com.deepak.NetworkDevices.dto.request.AllocateRequest;
import com.deepak.NetworkDevices.dto.request.CreateDeviceRequest;
import com.deepak.NetworkDevices.dto.request.UpdateDeviceRequest;
import com.deepak.NetworkDevices.dto.response.DeviceDto;
import com.deepak.NetworkDevices.dto.response.DeviceSummaryResponse;
import com.deepak.NetworkDevices.dto.response.ShelfPositionDto;
import com.deepak.NetworkDevices.service.DeviceService;
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

@WebMvcTest(DeviceController.class)
public class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    void getAllDevicesTest() throws Exception {
        // Arrange
        List<DeviceDto> listDevices = List.of(
                new DeviceDto("uuid1", "device1", "type1", "part1", "trill4", 3, null, null),
                new DeviceDto("uuid2", "device2", "type2", "part2", "trill4", 3, null, null)
        );
        when(deviceService.getDevices()).thenReturn(listDevices);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].deviceName").value("device1"))
                .andExpect(jsonPath("$[1].deviceId").value("uuid2"));

        verify(deviceService, times(1)).getDevices();
    }

    @Test
    void getAllDevices_ReturnsEmptyList_WhenNoDevicesExist() throws Exception {
        // Arrange: Force service to return an empty list
        when(deviceService.getDevices()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())  // Verify it is an array
                .andExpect(jsonPath("$").isEmpty()); // Verify the array has no elements

        verify(deviceService, times(1)).getDevices();
    }

    @Test
    void getDeviceByIdTest() throws Exception {
        // Arrange
        DeviceSummaryResponse deviceSummaryResponse = new DeviceSummaryResponse(
                new DeviceDto("uuid1", "device1", "type1", "part1", "trill4", 3, null, null),
                List.of(
                        new ShelfPositionDto("uuid3", 0, false, null),
                        new ShelfPositionDto("uuid4", 1, false, null),
                        new ShelfPositionDto("uuid5", 2, false, null)
                )
        );

        when(deviceService.getDeviceSummary("uuid1")).thenReturn(deviceSummaryResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/devices/uuid1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.device.deviceName").value("device1"))
                .andExpect(jsonPath("$.device.deviceId").value("uuid1"))
                .andExpect(jsonPath("$.device.buildingName").value("trill4"))
                .andExpect(jsonPath("$.positions[0].shelfPositionId").value("uuid3"))
                .andExpect(jsonPath("$.positions[1].index").value(1));

        verify(deviceService, times(1)).getDeviceSummary("uuid1");
    }

    @Test
    void createDeviceTest() throws Exception {
        // Arrange
        CreateDeviceRequest createDeviceRequest = new CreateDeviceRequest("device1", "type1", "part1", "trill4", 3);
        String generatedId = "uuid1";
        when(deviceService.createDevice(any(CreateDeviceRequest.class))).thenReturn(generatedId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deviceName": "device1",
                                    "deviceType": "type1",
                                    "partNumber": "part1",
                                    "buildingName": "trill4",
                                    "numberOfShelfPositions": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string(generatedId));

        verify(deviceService, times(1)).createDevice(any(CreateDeviceRequest.class));
    }

    @Test
    void updateDeviceTest() throws Exception {
        // Arrange
        UpdateDeviceRequest updateDeviceRequest = new UpdateDeviceRequest("device1-updated", "type1-updated",null,null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/devices/uuid1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "deviceName": "device1-updated",
                                    "deviceType": "type1-updated"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).updateDevice(eq("uuid1"), any(UpdateDeviceRequest.class));
    }

    @Test
    void deleteDeviceTest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/devices/uuid1"))
                .andExpect(status().isNoContent());

        verify(deviceService, times(1)).softDeleteDevice("uuid1");
    }

    @Test
    void allocateShelfPositionTest() throws Exception {
        // Arrange
        AllocateRequest allocateRequest = new AllocateRequest("shelf123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/devices/uuid1/shelf-positions/uuid2:allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "shelfId": "shelf123"
                            }
                            """))
                .andExpect(status().isOk());

        verify(deviceService, times(1)).allocate(eq("uuid1"), eq("uuid2"), any(AllocateRequest.class));
    }

    @Test
    void freeShelfPositionTest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/devices/uuid1/shelf-positions/uuid2:free"))
                .andExpect(status().isOk());

        verify(deviceService, times(1)).free("uuid1", "uuid2");
    }
}