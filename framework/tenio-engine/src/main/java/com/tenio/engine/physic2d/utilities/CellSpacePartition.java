package com.tenio.engine.physic2d.utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.tenio.engine.physic2d.common.BaseGameEntity;
import com.tenio.engine.physic2d.common.InvertedAABBox2D;
import com.tenio.engine.physic2d.graphic.Renderable;
import com.tenio.engine.physic2d.graphic.Paint;
import com.tenio.engine.physic2d.math.Vector2;

/**
 * This class is used to defines a cell containing a list of pointers to
 * entities
 */
class Cell<E extends Object> {
	/**
	 * All the entities inhabiting this cell
	 */
	public List<E> members = new LinkedList<E>();
	/**
	 * The cell's bounding box (it's inverted because the Window's default
	 * co-ordinate system has a y axis that increases as it descends)
	 */
	public InvertedAABBox2D bbox;

	public Cell(float left, float top, float right, float bottom) {
		bbox = InvertedAABBox2D.valueOf(left, top, right, bottom);
	}

}

/**
 * This class is used to divide a 2D space into a grid of cells each of which
 * may contain a number of entities. Once created and initialized with entities,
 * fast proximity queries can be made by calling the CalculateNeighbors method
 * with a position and proximity radius.
 *
 * If an entity is capable of moving, and therefore capable of moving between
 * cells, the Update method should be called each update-cycle to synchronize
 * the entity and the cell space it occupies
 * 
 * @param <E> the game entity template
 * 
 * @author sallyx (https://www.sallyx.org/sally/en/game-ai/)
 *
 */
public class CellSpacePartition<E extends BaseGameEntity> implements Renderable {

	/**
	 * Only for temporary calculations
	 */
	private final Vector2 __temp1 = Vector2.newInstance();
	private final InvertedAABBox2D __aabb = InvertedAABBox2D.newInstance();

	/**
	 * The required amount of cells in the space
	 */
	private List<Cell<E>> __cells = new ArrayList<Cell<E>>();
	/**
	 * This is used to store any valid neighbors when an agent searches its
	 * neighboring space
	 */
	private List<E> __neighbors;
	/**
	 * This iterator will be used by the methods next and begin to traverse through
	 * the above vector of neighbors
	 */
	private ListIterator<E> __currNeighbor;
	// The width and height of the world space the entities inhabit
	private float __spaceWidth;
	private float __spaceHeight;
	// The number of cells the space is going to be divided up into
	private int __numCellsX;
	private int __numCellsY;
	private float __cellSizeX;
	private float __cellSizeY;

	/**
	 * Create a new instance
	 * 
	 * @param width       width of 2D space
	 * @param height      height of 2D space
	 * @param cellsX      number of divisions horizontally
	 * @param cellsY      number of divisions vertically
	 * @param maxEntities maximum number of entities to partition
	 */
	public CellSpacePartition(float width, float height, int cellsX, int cellsY, int maxEntities) {
		__spaceWidth = width;
		__spaceHeight = height;
		__numCellsX = cellsX;
		__numCellsY = cellsY;
		__neighbors = new ArrayList<E>(maxEntities);
		// calculate bounds of each cell
		__cellSizeX = width / cellsX;
		__cellSizeY = height / cellsY;

		// create the cells
		for (int y = 0; y < __numCellsY; ++y) {
			for (int x = 0; x < __numCellsX; ++x) {
				float left = x * __cellSizeX;
				float right = left + __cellSizeX;
				float top = y * __cellSizeY;
				float bot = top + __cellSizeY;

				__cells.add(new Cell<E>(left, top, right, bot));
			}
		}
	}

	/**
	 * Given a 2D vector representing a position within the game world, this method
	 * calculates an index into its appropriate cell
	 * 
	 * @param position the desired position
	 * @return the index
	 */
	private int getIndexByPosition(Vector2 position) {
		int idx = (int) (__numCellsX * position.x / __spaceWidth)
				+ ((int) ((__numCellsY) * position.y / __spaceHeight) * __numCellsX);

		// if the entity's position is equal to vector2d(m_dSpaceWidth, m_dSpaceHeight)
		// then the index will overshoot. We need to check for this and adjust
		if (idx > (int) __cells.size() - 1) {
			idx = (int) __cells.size() - 1;
		}

		return idx;
	}

	/**
	 * Used to add the entities to the data structure adds entities to the class by
	 * allocating them to the appropriate cell
	 * 
	 * @param entity an entity
	 */
	public void addEntity(E entity) {
		int idx = getIndexByPosition(entity.getPosition());
		__cells.get(idx).members.add(entity);
	}

	/**
	 * Update an entity's cell by calling this from your entity's Update method
	 * Checks to see if an entity has moved cells. If so the data structure is
	 * updated accordingly
	 * 
	 * @param entity      an entity
	 * @param oldPosition see {@link Vector2}
	 */
	public void updateEntity(E entity, Vector2 oldPosition) {
		// if the index for the old position and the new position are not equal then
		// the entity has moved to another cell.
		int oldIdx = getIndexByPosition(oldPosition);
		int newIdx = getIndexByPosition(entity.getPosition());

		if (newIdx == oldIdx) {
			return;
		}

		// the entity has moved into another cell so delete from current cell
		// and add to new one
		__cells.get(oldIdx).members.remove(entity);
		__cells.get(newIdx).members.add(entity);
	}

	/**
	 * This must be called to create the vector of neighbors.This method examines
	 * each cell within range of the target, If the cells contain entities then they
	 * are tested to see if they are situated within the target's neighborhood
	 * region. If they are they are added to neighbor list
	 *
	 * this method stores a target's neighbors in the neighbor vector. After you
	 * have called this method use the begin, next and end methods to iterate
	 * through the vector.
	 * 
	 * @param targetPos   see {@link Vector2}
	 * @param queryRadius radius value
	 */
	public void calculateNeighbors(Vector2 targetPos, float queryRadius) {
		__neighbors.clear();

		// create the query box that is the bounding box of the target's query area
		__temp1.set(targetPos).sub(queryRadius, queryRadius);
		__aabb.setLeft(__temp1.x);
		__aabb.setTop(__temp1.y);
		__temp1.set(targetPos).add(queryRadius, queryRadius);
		__aabb.setRight(__temp1.x);
		__aabb.setBottom(__temp1.y);

		// iterate through each cell and test to see if its bounding box overlaps
		// with the query box. If it does and it also contains entities then
		// make further proximity tests.
		var c_it = __cells.listIterator();
		while (c_it.hasNext()) {
			var curCell = c_it.next();
			// test to see if this cell contains members and if it overlaps the
			// query box
			if (curCell.bbox.isOverlappedWith(__aabb) && !curCell.members.isEmpty()) {

				// add any entities found within query radius to the neighbor list
				var it = curCell.members.listIterator();
				while (it.hasNext()) {
					E ent = it.next();
					if (__temp1.set(ent.getPosition()).getDistanceSqrValue(targetPos) < queryRadius * queryRadius) {
						__neighbors.add(ent);
					}
				}
			}
		} // next cell
	}

	/**
	 * @return a reference to the entity at the front of the neighbor vector
	 */
	public E getFrontOfNeighbor() {
		__currNeighbor = __neighbors.listIterator();
		if (!__currNeighbor.hasNext()) {
			return null;
		}
		return __currNeighbor.next();
	}

	/**
	 * @return the next entity in the neighbor vector
	 */
	public E getNextOfNeighbor() {
		if (__currNeighbor == null || !__currNeighbor.hasNext()) {
			return null;
		}
		return __currNeighbor.next();
	}

	/**
	 * @return <b>true</b> if the end of the vector is found (a zero value marks the
	 *         end)
	 */
	public boolean isEndOfNeighbors() {
		return (__currNeighbor == null || (!__currNeighbor.hasNext()));
	}

	/**
	 * Clears the cells of all entities
	 */
	public void clearCells() {
		var it = __cells.listIterator();
		while (it.hasNext()) {
			it.next().members.clear();
		}
	}

	@Override
	public void render(Paint paint) {
		var curCell = __cells.listIterator();
		while (curCell.hasNext()) {
			curCell.next().bbox.render(paint);
		}
	}

}
