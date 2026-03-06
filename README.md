# Network Devices
A Demo Project simulating the layered architecture of Networks vis simulating Device,ShelfPosition and Shelf and how it interacts with each other.

```
(Device)-[:HAS]->(ShelfPosition)
(ShelfPosition)-[:HAS]->(Shelf)
```
### Features:
1. Support CRUD operation for both Devices and Shelves and ShelfPosition created at the time of Device Creation only.
2. Shelf can be added and freed from a Shelf Position.

## Ket Tech Stacks Used:
* Java Spring Boot for Backend
* Angular for Frontend
* Neo4j Database

### How to Run the Project:
* Git Clone the Project
* Setup the Database 
* Run Backend check CORS enabled
* Run Frontend now should be able to see output in browser

### Example Dashboard :
* Home/Devices Page:
![Home Page](/images/devicePage.png)
* Create Device Form:
![Create Device Form](/images/create%20DeviceForm.png)
* Edit Device Page:
![Edit Device Page](/images/EditDeviceForm.png)
* Shelf Page:
![Shelf Page](/images/shelfPage.png)
* Allocate Shelf to ShelfPosition Page:
![Allocation Services](/images/allocation.png)
