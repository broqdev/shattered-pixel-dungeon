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

package com.watabou.noosa;

import com.badlogic.gdx.Gdx;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Attribute;
import com.watabou.glwrap.Quad;
import com.watabou.glwrap.Uniform;
import com.watabou.glwrap.Vertexbuffer;
import com.watabou.utils.DeviceCompat;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NoosaScript extends Script {
	
	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;
	public Attribute aXY;
	public Attribute aUV;
	
	private Camera lastCamera;
	private Vertexbuffer clientBuffer;
	private int clientBufferCapacity;
	private int clientIndexBuffer = -1;
	private int clientIndexBufferCapacity;
	
	public NoosaScript() {

		super();
		compile( shader() );
		
		uCamera	= uniform( "uCamera" );
		uModel	= uniform( "uModel" );
		uTex	= uniform( "uTex" );
		uColorM	= uniform( "uColorM" );
		uColorA	= uniform( "uColorA" );
		aXY		= attribute( "aXYZW" );
		aUV		= attribute( "aUV" );

		Quad.setupIndices();
		Quad.bindIndices();
		
	}
	
	@Override
	public void use() {
		
		super.use();
		
		aXY.enable();
		aUV.enable();
		Quad.bindIndices();
		
	}

	public void drawElements( FloatBuffer vertices, ShortBuffer indices, int size ) {

		bindClientVertices( vertices );

		if (DeviceCompat.isWeb()) {
			bindClientIndices( indices, size );
			Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, size, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
		} else {
			Quad.releaseIndices();
			Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, size, Gdx.gl20.GL_UNSIGNED_SHORT, indices );
		}
		Quad.bindIndices();
	}

	public void drawQuad( FloatBuffer vertices ) {

		bindClientVertices( vertices );

		Quad.bindIndices();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	public void drawQuad( Vertexbuffer buffer ) {

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		buffer.release();

		Quad.bindIndices();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}
	
	public void drawQuadSet( FloatBuffer vertices, int size ) {
		
		if (size == 0) {
			return;
		}

		bindClientVertices( vertices );

		Quad.bindIndices();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * size, Gdx.gl20.GL_UNSIGNED_SHORT, 0 );
	}

	private void bindClientVertices( FloatBuffer vertices ) {
		((Buffer)vertices).position( 0 );
		if (clientBuffer == null || clientBuffer.isDeleted() || clientBufferCapacity < vertices.limit()) {
			if (clientBuffer != null && !clientBuffer.isDeleted()) {
				clientBuffer.delete();
			}
			clientBuffer = new Vertexbuffer( vertices );
			clientBufferCapacity = vertices.limit();
		} else {
			clientBuffer.updateVertices( vertices );
		}

		clientBuffer.updateGLData();
		clientBuffer.bind();
		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );
		clientBuffer.release();
	}

	public void drawQuadSet( Vertexbuffer buffer, int length, int offset ){

		if (length == 0) {
			return;
		}

		buffer.updateGLData();

		buffer.bind();

		aXY.vertexBuffer( 2, 4, 0 );
		aUV.vertexBuffer( 2, 4, 2 );

		buffer.release();

		Quad.bindIndices();
		
		Gdx.gl20.glDrawElements( Gdx.gl20.GL_TRIANGLES, Quad.SIZE * length, Gdx.gl20.GL_UNSIGNED_SHORT, Quad.SIZE * Short.SIZE/8 * offset );
	}

	private void bindClientIndices( ShortBuffer indices, int size ) {
		if (clientIndexBuffer == -1) {
			clientIndexBuffer = Gdx.gl20.glGenBuffer();
		}

		ShortBuffer update = slice( indices, 0, size );
		Gdx.gl20.glBindBuffer( Gdx.gl20.GL_ELEMENT_ARRAY_BUFFER, clientIndexBuffer );
		if (clientIndexBufferCapacity < size) {
			Gdx.gl20.glBufferData( Gdx.gl20.GL_ELEMENT_ARRAY_BUFFER, size * Short.SIZE/8, update, Gdx.gl20.GL_DYNAMIC_DRAW );
			clientIndexBufferCapacity = size;
		} else {
			Gdx.gl20.glBufferSubData( Gdx.gl20.GL_ELEMENT_ARRAY_BUFFER, 0, size * Short.SIZE/8, update );
		}
	}

	private static ShortBuffer slice( ShortBuffer buffer, int start, int end ) {
		ShortBuffer update = buffer.duplicate();
		((Buffer)update).position( start );
		((Buffer)update).limit( end );
		return update.slice();
	}

	@Override
	public void delete() {
		super.delete();
		if (clientBuffer != null && !clientBuffer.isDeleted()) {
			clientBuffer.delete();
			clientBuffer = null;
		}
		if (clientIndexBuffer != -1) {
			Gdx.gl20.glDeleteBuffer( clientIndexBuffer );
			clientIndexBuffer = -1;
			clientIndexBufferCapacity = 0;
		}
	}
	
	public void lighting( float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa ) {
		uColorM.value4f( rm, gm, bm, am );
		uColorA.value4f( ra, ga, ba, aa );
	}
	
	public void resetCamera() {
		lastCamera = null;
	}
	
	public void camera( Camera camera ) {
		if (camera == null) {
			camera = Camera.main;
		}
		if (camera != lastCamera && camera.matrix != null) {
			lastCamera = camera;
			uCamera.valueM4( camera.matrix );

			if (!camera.fullScreen) {
				Gdx.gl20.glEnable( Gdx.gl20.GL_SCISSOR_TEST );

				//This fixes pixel scaling issues on some hidpi displays (mainly on macOS)
				// because for some reason all other openGL operations work on virtual pixels
				// but glScissor operations work on real pixels
				float xScale = DeviceCompat.getRealPixelScaleX();
				float yScale = DeviceCompat.getRealPixelScaleY();

				Gdx.gl20.glScissor(
						Math.round(camera.x * xScale),
						Math.round((Game.height - camera.screenHeight - camera.y) * yScale),
						Math.round(camera.screenWidth * xScale),
						Math.round(camera.screenHeight * yScale));
			} else {
				Gdx.gl20.glDisable( Gdx.gl20.GL_SCISSOR_TEST );
			}
		}
	}
	
	public static NoosaScript get() {
		return Script.use( NoosaScript.class );
	}
	
	
	protected String shader() {
		return SHADER;
	}
	
	private static final String SHADER =
		
		//vertex shader
		"uniform mat4 uCamera;\n" +
		"uniform mat4 uModel;\n" +
		"attribute vec4 aXYZW;\n" +
		"attribute vec2 aUV;\n" +
		"varying vec2 vUV;\n" +
		"void main() {\n" +
		"  gl_Position = uCamera * uModel * aXYZW;\n" +
		"  vUV = aUV;\n" +
		"}\n" +
		
		//this symbol separates the vertex and fragment shaders (see Script.compile)
		"//\n" +
		
		//fragment shader
		//preprocessor directives let us define precision on GLES platforms, and ignore it elsewhere
		"#ifdef GL_ES\n" +
		"  precision mediump float;\n" +
		"#endif\n" +
		"varying vec2 vUV;\n" +
		"uniform sampler2D uTex;\n" +
		"uniform vec4 uColorM;\n" +
		"uniform vec4 uColorA;\n" +
		"void main() {\n" +
		"  gl_FragColor = texture2D( uTex, vUV ) * uColorM + uColorA;\n" +
		"}\n";
}
