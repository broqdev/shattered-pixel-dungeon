/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.glwrap;

import com.badlogic.gdx.Gdx;
import com.watabou.utils.DeviceCompat;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Vertexbuffer {

	private int id;
	private FloatBuffer vertices;
	private int updateStart, updateEnd;
	private boolean deleted;

	private static final ArrayList<Vertexbuffer> buffers = new ArrayList<>();

	public Vertexbuffer( FloatBuffer vertices ) {
		synchronized (buffers) {
			id = Gdx.gl.glGenBuffer();

			this.vertices = vertices;
			buffers.add(this);

			updateStart = 0;
			updateEnd = vertices.limit();
		}
	}

	//For flagging the buffer for a full update without changing anything
	public void updateVertices(){
		updateVertices(vertices);
	}

	//For flagging an update with a full set of new data
	public void updateVertices( FloatBuffer vertices ){
		updateVertices(vertices, 0, vertices.limit());
	}

	//For flagging an update with a subset of data changed
	public void updateVertices( FloatBuffer vertices, int start, int end){
		this.vertices = vertices;

		if (updateStart == -1)
			updateStart = start;
		else
			updateStart = Math.min(start, updateStart);

		if (updateEnd == -1)
			updateEnd = end;
		else
			updateEnd = Math.max(end, updateEnd);
	}

	public void updateGLData(){
		if (updateStart == -1) return;

		((Buffer)vertices).position(updateStart);
		bind();

		if (updateStart == 0 && updateEnd == vertices.limit()){
			FloatBuffer update = DeviceCompat.isWeb() ? slice(vertices, 0, vertices.limit()) : vertices;
			((Buffer)update).position(0);
			Gdx.gl.glBufferData(Gdx.gl.GL_ARRAY_BUFFER, update.limit()*4, update, Gdx.gl.GL_DYNAMIC_DRAW);
		} else if (DeviceCompat.isWeb()) {
			// TeaVM's WebGL bridge exposes the whole Buffer view to bufferSubData,
			// ignoring the Java Buffer position and size args. A sliced view keeps
			// web partial updates aligned with desktop GL semantics.
			FloatBuffer update = slice(vertices, updateStart, updateEnd);
			Gdx.gl.glBufferSubData(Gdx.gl.GL_ARRAY_BUFFER, updateStart*4, (updateEnd - updateStart)*4, update);
		} else {
			Gdx.gl.glBufferSubData(Gdx.gl.GL_ARRAY_BUFFER, updateStart*4, (updateEnd - updateStart)*4, vertices);
		}

		release();
		updateStart = updateEnd = -1;
	}

	private static FloatBuffer slice(FloatBuffer buffer, int start, int end) {
		FloatBuffer update = buffer.duplicate();
		((Buffer)update).position(start);
		((Buffer)update).limit(end);
		return update.slice();
	}

	public void bind(){
		Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, id);
	}

	public void release(){
		Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, 0);
	}

	public void delete(){
		synchronized (buffers) {
			Gdx.gl.glDeleteBuffer( id );
			buffers.remove(this);
			deleted = true;
		}
	}

	public boolean isDeleted(){
		return deleted;
	}

	public static void clear(){
		synchronized (buffers) {
			for (Vertexbuffer buf : buffers.toArray(new Vertexbuffer[0])) {
				buf.delete();
			}
		}
	}

	public static void reload(){
		synchronized (buffers) {
			for (Vertexbuffer buf : buffers) {
				buf.updateVertices();
				buf.updateGLData();
			}
		}
	}

}
