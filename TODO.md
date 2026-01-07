# Game Development TODO

## Milestone 1: Basic Controllable Unit
Goal: Create a game with a flat world and one unit that can be controlled with mouse

### High-Level Implementation Steps

#### 1. Set up LibGDX project
- Use gdx-liftoff or libGDX project generator
- Select desktop (lwjgl3) platform for development
- Choose appropriate package name

#### 2. Create basic game structure
- Main game class extending ApplicationAdapter or Game
- GameScreen for our main gameplay
- Basic render loop setup

#### 3. Set up orthographic camera
- Top-down 2D view
- Camera controls for viewport
- Screen-to-world coordinate conversion

#### 4. Create flat world with grid
- Simple tile-based grid rendering
- Ground tiles (can be just colored rectangles for now)
- Define world size (e.g., 50x50 tiles)

#### 5. Create Unit class
- Position (x, y)
- Visual representation (sprite or simple shape)
- Render method

#### 6. Implement mouse input
- Detect mouse clicks
- Convert screen coordinates to world coordinates
- Handle left-click for movement commands

#### 7. Add movement logic
- Move unit from current position to clicked position
- Simple linear interpolation or direct movement (no pathfinding yet)
- Update unit position each frame

#### 8. Test everything
- Verify unit appears on screen
- Verify clicking moves the unit
- Check camera follows or displays correctly

---

## Future Milestones (Planned)

### Milestone 2: Multiple Units & Selection
- Multiple units on map
- Unit selection with mouse
- Group selection
- Movement commands for selected units

### Milestone 3: Grid-Based Pathfinding
- Implement A* pathfinding
- Units navigate around obstacles
- Add basic obstacles to the world

### Milestone 4: Zombies & Flow Fields
- Add zombie entities
- Implement flow field pathfinding for zombies
- Zombies move toward player base

### Milestone 5: Building & Defense
- Add building placement
- Tower defense structures
- Resource management (optional)

### Milestone 6: Networking
- Server-client architecture
- Multiplayer synchronization
- Co-op gameplay
