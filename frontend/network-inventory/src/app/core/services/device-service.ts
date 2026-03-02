import { Injectable, inject } from '@angular/core';
import { HttpClient , HttpHeaders, HttpResponse} from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Device, DeviceSummary, CreateDevicePayload, UpdateDevicePayload, AllocateShelfPayload } from '../models/device';

@Injectable({
  providedIn: 'root',
})
export class DeviceService {
  private readonly http = inject(HttpClient);
  // Using http:// is important for local development to avoid protocol issues
  private readonly apiUrl = 'http://localhost:8080/api/v1/devices';

  getDevices(): Observable<Device[]> {
    const url = `${this.apiUrl}/all`;
    return this.http.get<Device[]>(url);
  }

  getDeviceSummary(deviceId: string) {
  const url = `${this.apiUrl}/${deviceId}/summary`;
  return this.http.get<DeviceSummary>(url).pipe(
    map(summary => ({
      ...summary,
      positions: summary.positions.sort((a, b) => a.index - b.index)
    }))
  );
}


allocateShelf(deviceId: string, shelfPositionId: string, shelfId: string) {
    const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}:allocate`;
    return this.http.post<void>(url, { shelfId }, { observe: 'response' })
      .pipe(map(() => void 0));
  }


freeShelfPosition(deviceId: string, shelfPositionId: string) {
  const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}:free`;
  return this.http.post<void>(url, {}, { observe: 'response' }).pipe(map(() => void 0));
}
 
  
  

  createDevice(payload: CreateDevicePayload): Observable<string> {
      return this.http.post(this.apiUrl, payload, {
        headers: new HttpHeaders({
          'Content-Type': 'application/json',
          'Accept': 'text/plain',           
        }),
        responseType: 'text',               
        observe: 'response',                
      }).pipe(
        map((res: HttpResponse<string>) => (res.body ?? '').trim())
      );
  }




  
  
updateDevice(deviceId: string, payload: UpdateDevicePayload) {
    return this.http.patch(`${this.apiUrl}/${deviceId}`, payload, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'text/plain',
      }),
      responseType: 'text',
      observe: 'response',
    }).pipe(map(res => (res.body ?? '').trim()));
  }

  deleteDevice(deviceId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${deviceId}`);
  }

  
  /*freeShelfPosition(deviceId: string, shelfPositionId: string): Observable<void> {
    const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}/:free`;
    return this.http.post<void>(url, {});
  }

  
  allocateShelf(deviceId: string, shelfPositionId: string, shelfId: string): Observable<void> {
    const url = `${this.apiUrl}/${deviceId}/shelf-positions/${shelfPositionId}/allocate`;
    const payload: AllocateShelfPayload = { shelfId,shelfPositionId, deviceId };
    return this.http.post<void>(url, payload);
  }*/
}