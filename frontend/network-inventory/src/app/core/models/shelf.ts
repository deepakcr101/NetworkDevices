// src/app/core/models/shelf.model.ts
export interface Shelf {
  shelfId: string;
  shelfName: string;
  
  partName: string;
  
  // These properties identify where the shelf is located.
  // They are optional because a shelf might not be positioned yet.
  deviceId?: string;
  shelfPositionId?: string;
  status: string;
}