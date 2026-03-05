package com.deepak.NetworkDevices.service;

import com.deepak.NetworkDevices.dto.request.CreateShelfRequest;
import com.deepak.NetworkDevices.dto.request.UpdateShelfRequest;
import com.deepak.NetworkDevices.dto.response.ShelfDto;
import com.deepak.NetworkDevices.dto.response.ShelfLiteDto;

import java.util.List;

public interface ShelfService {
    String createShelf(CreateShelfRequest req);
    ShelfDto getShelf(String shelfId);
    void updateShelf(String shelfId, UpdateShelfRequest req);
    void softDeleteShelf(String shelfId);
    List<ShelfDto> listShelvesWithStatus();
    List<ShelfLiteDto> listAvailableShelves();
}