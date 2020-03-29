/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.engine.ecs.common;

/**
 * An entity is something that exists in your game world. Again, an entity is
 * little more than a list of components. Because they are so simple, most
 * implementations won't define an entity as a concrete piece of data. Instead,
 * an entity is a unique ID, and all components that make up an entity will be
 * tagged with that ID.
 * 
 * @author Kong
 */
public interface IEntity {
	
	void setEnabled(boolean enabled);
	
	boolean isEnabled();

	void addComponent(int index, IComponent component);

	void removeComponent(int index);

	void replaceComponent(int index, IComponent component);

	IComponent getComponent(int index);

	IComponent[] getComponents();

	boolean hasComponent(int index);

	boolean hasComponents(int... indices);

	boolean hasAnyComponent(int... indices);

	void removeAllComponents();
	
	void reset();

}
