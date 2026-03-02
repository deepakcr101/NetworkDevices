// src/app/core/models/device.model.ts
import { Shelf } from './shelf';

export type ShelfPositionStatus = 'OCCUPIED' | 'FREE';

export interface ShelfPosition {
  shelfPositionId: string;
  index: number;
  isOccupied: boolean;
  shelf?: Shelf; // Only present if status is 'OCCUPIED'
}

export interface Device {
  deviceId: string;
  deviceName: string;
  deviceType: string;
  partNumber: string;
  buildingName: string;
  numberOfShelfPositions: number;
  
}


export interface DeviceSummary {
  device: Device;
  positions: ShelfPosition[];
}


export interface CreateDevicePayload {
  deviceName: string;
  deviceType: string;
  partNumber: string;
  buildingName: string;
  numberOfShelfPositions: number;
}


// For updating a device. The properties are optional.
export type UpdateDevicePayload = Partial<{
  deviceName: string;
  deviceType: string;
  partNumber: string;
  buildingName: string;
}>;

// For allocating a shelf. Matches the backend's AllocateRequest.
export type AllocateShelfPayload = {
  deviceId: string;
  shelfPositionId: string;
  shelfId: string;
};