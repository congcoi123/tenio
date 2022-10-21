package com.tenio.engine.physic2d.utility;

import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.InvertedAabbBox2D;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * This class is used to divide a 2D space into a grid of cells each of which
 * may contain a number of entities. Once created and initialized with entities,
 * fast proximity queries can be made by calling the CalculateNeighbors method
 * with a position and proximity radius.
 * <br>
 * If an entity is capable of moving, and therefore capable of moving between
 * cells, the Update method should be called each update-cycle to synchronize
 * the entity and the cell space it occupies
 *
 * @param <T> the game entity template
 * @author sallyx (https://www.sallyx.org/sally/en/game-ai/)
 */
public class CellSpacePartition<T extends BaseGameEntity> implements Renderable {

  /**
   * Only for temporary calculations.
   */
  private final InvertedAabbBox2D aabbBox2D = InvertedAabbBox2D.newInstance();

  /**
   * The required amount of cells in the space.
   */
  private final List<Cell<T>> cells = new ArrayList<>();
  /**
   * This is used to store any valid neighbors when an agent searches its
   * neighboring space.
   */
  private final List<T> neighbors;
  // The width and height of the world space the entities inhabit
  private final float spaceWidth;
  private final float spaceHeight;
  // The number of cells the space is going to be divided up into
  private final int numCellsX;
  private final int numCellsY;
  /**
   * This iterator will be used by the methods next and begin to traverse through
   * the above vector of neighbors.
   */
  private ListIterator<T> currNeighbor;

  /**
   * Create a new instance.
   *
   * @param width       width of 2D space
   * @param height      height of 2D space
   * @param cellsX      number of divisions horizontally
   * @param cellsY      number of divisions vertically
   * @param maxEntities maximum number of entities to partition
   */
  public CellSpacePartition(float width, float height, int cellsX, int cellsY, int maxEntities) {
    spaceWidth = width;
    spaceHeight = height;
    numCellsX = cellsX;
    numCellsY = cellsY;
    neighbors = new ArrayList<>(maxEntities);
    // calculate bounds of each cell
    float cellSizeX = width / cellsX;
    float cellSizeY = height / cellsY;

    // create the cells
    for (int y = 0; y < numCellsY; ++y) {
      for (int x = 0; x < numCellsX; ++x) {
        float left = x * cellSizeX;
        float right = left + cellSizeX;
        float top = y * cellSizeY;
        float bot = top + cellSizeY;

        cells.add(new Cell<>(left, top, right, bot));
      }
    }
  }

  /**
   * Given a 2D vector representing a position within the game world, this method
   * calculates an index into its appropriate cell.
   *
   * @param position the desired position
   * @return the index
   */
  private int getIndexByPosition(Vector2 position) {
    int idx = (int) (numCellsX * position.x / spaceWidth)
        + ((int) ((numCellsY) * position.y / spaceHeight) * numCellsX);

    // if the entity's position is equal to vector2d(m_dSpaceWidth, m_dSpaceHeight)
    // then the index will overshoot. We need to check for this and adjust
    if (idx > cells.size() - 1) {
      idx = cells.size() - 1;
    }

    return idx;
  }

  /**
   * Used to add the entities to the data structure adds entities to the class by
   * allocating them to the appropriate cell.
   *
   * @param entity an entity
   */
  public void addEntity(T entity) {
    int idx = getIndexByPosition(entity.getPosition());
    cells.get(idx).members.add(entity);
  }

  /**
   * Update an entity's cell by calling this from your entity's Update method
   * Checks to see if an entity has moved cells. If so the data structure is
   * updated accordingly.
   *
   * @param entity      an entity
   * @param oldPosition see {@link Vector2}
   */
  public void updateEntity(T entity, Vector2 oldPosition) {
    // if the index for the old position and the new position are not equal then
    // the entity has moved to another cell.
    int oldIdx = getIndexByPosition(oldPosition);
    int newIdx = getIndexByPosition(entity.getPosition());

    if (newIdx == oldIdx) {
      return;
    }

    // the entity has moved into another cell so delete from current cell
    // and add to new one
    cells.get(oldIdx).members.remove(entity);
    cells.get(newIdx).members.add(entity);
  }

  /**
   * This must be called to create the vector of neighbors.This method examines
   * each cell within range of the target, If the cells contain entities then they
   * are tested to see if they are situated within the target's neighborhood
   * region. If they are added to neighbor list
   * <br>
   * this method stores a target's neighbors in the neighbor vector. After you
   * have called this method use the begin, next and end methods to iterate
   * through the vector.
   *
   * @param targetPos   see {@link Vector2}
   * @param queryRadius radius value
   */
  public void calculateNeighbors(Vector2 targetPos, float queryRadius) {
    neighbors.clear();

    // create the query box that is the bounding box of the target's query area
    var temp = Vector2.newInstance().set(targetPos).sub(queryRadius, queryRadius);
    aabbBox2D.setLeft(temp.x);
    aabbBox2D.setTop(temp.y);
    temp.set(targetPos).add(queryRadius, queryRadius);
    aabbBox2D.setRight(temp.x);
    aabbBox2D.setBottom(temp.y);

    // iterate through each cell and test to see if its bounding box overlaps
    // with the query box. If it does, and it also contains entities then
    // make further proximity tests.
    var cellListIterator = cells.listIterator();
    while (cellListIterator.hasNext()) {
      var curCell = cellListIterator.next();
      // test to see if this cell contains members and if it overlaps the
      // query box
      if (curCell.bbox.isOverlappedWith(aabbBox2D) && !curCell.members.isEmpty()) {

        // add any entities found within query radius to the neighbor list
        var it = curCell.members.listIterator();
        while (it.hasNext()) {
          T ent = it.next();
          if (temp.set(ent.getPosition()).getDistanceSqrValue(targetPos)
              < queryRadius * queryRadius) {
            neighbors.add(ent);
          }
        }
      }
    } // next cell
  }

  /**
   * Retrieves a reference to the entity at the front of the neighbor vector.
   *
   * @return a reference to the entity at the front of the neighbor vector
   */
  public T getFrontOfNeighbor() {
    currNeighbor = neighbors.listIterator();
    if (!currNeighbor.hasNext()) {
      return null;
    }
    return currNeighbor.next();
  }

  /**
   * Retrieves the next entity in the neighbor vector.
   *
   * @return the next entity in the neighbor vector
   */
  public T getNextOfNeighbor() {
    if (Objects.isNull(currNeighbor) || !currNeighbor.hasNext()) {
      return null;
    }
    return currNeighbor.next();
  }

  /**
   * Check if the end of vector is found.
   *
   * @return <b>true</b> if the end of the vector is found (a zero value marks the end)
   */
  public boolean isEndOfNeighbors() {
    return (Objects.isNull(currNeighbor) || (!currNeighbor.hasNext()));
  }

  /**
   * Clears the cells of all entities.
   */
  public void clearCells() {
    var it = cells.listIterator();
    while (it.hasNext()) {
      it.next().members.clear();
    }
  }

  @Override
  public void render(Paint paint) {
    var curCell = cells.listIterator();
    while (curCell.hasNext()) {
      curCell.next().bbox.render(paint);
    }
  }
}
