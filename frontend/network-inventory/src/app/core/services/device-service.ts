import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Device, DeviceSummary } from '../models/device';

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  private readonly http = inject(HttpClient);
  
  private readonly apiUrl = '//localhost:8080/api/v1/devices';

  getDevices(): Observable<Device[]> {
    const url=`${this.apiUrl}/all`;
    return this.http.get<Device[]>(url);
  }

  
 getDeviceSummary(deviceId: string): Observable<DeviceSummary> {
    const url = `${this.apiUrl}/${deviceId}/summary`;
    return this.http.get<DeviceSummary>(url);  
  }
 
  /*return this.http.get<any>(`/api/devices/${deviceId}`).pipe(
      map(data => ({
        ...data,
        // Sort the nested 'positions' array by 'index'
        positions: data.positions.toSorted((a, b) => a.index - b.index)
      }))
    );*/

  createDevice(deviceData: Omit<Device, 'id' | 'shelfPositions'>): Observable<Device> {
    return this.http.post<Device>(this.apiUrl, deviceData);
  }

  freeShelfPosition(deviceId: string, shelfPositionId: string): Observable<DeviceSummary> {
    const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}/free`;
    return this.http.post<DeviceSummary>(url, {});
  }

  allocateShelf(deviceId: string, shelfPositionId: string, shelfId: string): Observable<Device> {
    const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}/allocate`;
    return this.http.post<Device>(url, { shelfId });
  }
}
