package com.deepak.NetworkDevices.service;

import com.deepak.NetworkDevices.dto.request.CreateDeviceRequest;
import com.deepak.NetworkDevices.dto.request.UpdateDeviceRequest;
import com.deepak.NetworkDevices.dto.request.AllocateRequest;
import com.deepak.NetworkDevices.dto.response.DeviceDto;
import com.deepak.NetworkDevices.dto.response.DeviceSummaryResponse;

import java.util.List;

public interface DeviceService {
    List<DeviceDto>  getDevices();
    String createDevice(CreateDeviceRequest req);
    DeviceSummaryResponse getDeviceSummary(String deviceId);
    void updateDevice(String deviceId, UpdateDeviceRequest req);
    void softDeleteDevice(String deviceId);
    void allocate(String deviceId, String shelfPositionId, AllocateRequest req);
    void free(String deviceId, String shelfPositionId);
}
