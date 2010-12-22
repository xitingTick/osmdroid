// Created by plusminus on 21:46:22 - 25.09.2008
package org.andnav.osm.tileprovider;

import org.andnav.osm.tileprovider.renderer.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * This is an abstract class. The tile provider is responsible for:
 * <ul>
 * <li>determining if a map tile is available,</li>
 * <li>notifying the client, via a callback handler</li>
 * </ul>
 * see {@link OpenStreetMapTile} for an overview of how tiles are served by this
 * provider.
 *
 * @author Nicolas Gramlich
 *
 */
public abstract class OpenStreetMapTileProviderBase implements
		IOpenStreetMapTileProviderCallback, OpenStreetMapViewConstants {

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStreetMapTileProviderBase.class);

	protected final OpenStreetMapTileCache mTileCache;
	protected Handler mTileRequestCompleteHandler;
	protected boolean mUseDataConnection = true;

	public abstract Drawable getMapTile(OpenStreetMapTile pTile);

	public abstract void detach();

	public abstract int getMinimumZoomLevel();

	public abstract int getMaximumZoomLevel();

	public abstract void setPreferredRenderer(
			IOpenStreetMapRendererInfo renderer);

	public abstract IOpenStreetMapRendererInfo getPreferredRenderer();

	public OpenStreetMapTileProviderBase() {
		this(null);
	}

	public OpenStreetMapTileProviderBase(final Handler pDownloadFinishedListener) {
		mTileCache = new OpenStreetMapTileCache();
		mTileRequestCompleteHandler = pDownloadFinishedListener;
	}

	/**
	 * Called by implementation class methods indicating that they have
	 * completed the request as best it can. The tile is added to the cache, and
	 * a MAPTILE_SUCCESS_ID message is sent.
	 *
	 * @param pState
	 *            the map tile request state object
	 * @param pDrawable
	 *            the Drawable of the map tile
	 */
	@Override
	public void mapTileRequestCompleted(
			final OpenStreetMapTileRequestState pState, final Drawable pDrawable) {
		OpenStreetMapTile tile = pState.getMapTile();
		if (pDrawable != null) {
			mTileCache.putTile(tile, pDrawable);
		}

		// tell our caller we've finished and it should update its view
		if (mTileRequestCompleteHandler != null)
			mTileRequestCompleteHandler
					.sendEmptyMessage(OpenStreetMapTile.MAPTILE_SUCCESS_ID);

		if (DEBUGMODE)
			logger.debug("MapTile request complete: " + tile);
	}

	/**
	 * Default implementation is to call mapTileRequestCompleted
	 *
	 * @param pState
	 *            the map tile request state object
	 * @param pDrawable
	 *            the Drawable of the map tile
	 */
	@Override
	public void mapTileRequestCandidate(OpenStreetMapTileRequestState aState,
			final Drawable aDrawable) {
		mapTileRequestCompleted(aState, aDrawable);
	}

	/**
	 * Called by implementation class methods indicating that they have failed
	 * to retrieve the requested map tile. a MAPTILE_FAIL_ID message is sent.
	 *
	 * @param pState
	 *            the map tile request state object
	 */
	@Override
	public void mapTileRequestFailed(final OpenStreetMapTileRequestState pState) {
		OpenStreetMapTile tile = pState.getMapTile();
		if (mTileRequestCompleteHandler != null)
			mTileRequestCompleteHandler
					.sendEmptyMessage(OpenStreetMapTile.MAPTILE_FAIL_ID);

		if (DEBUGMODE)
			logger.debug("MapTile request failed: " + tile);
	}

	public void setTileRequestCompleteHandler(Handler handler) {
		mTileRequestCompleteHandler = handler;
	}

	public void ensureCapacity(final int aCapacity) {
		mTileCache.ensureCapacity(aCapacity);
	}

	public void clearTileCache() {
		mTileCache.clear();
	}

	/**
	 * Whether to use the network connection if it's available.
	 */
	@Override
	public boolean useDataConnection() {
		return mUseDataConnection;
	}

	/**
	 * Set whether to use the network connection if it's available.
	 *
	 * @param aMode
	 *            if true use the network connection if it's available. if false
	 *            don't use the network connection even if it's available.
	 */
	public void setUseDataConnection(boolean aMode) {
		mUseDataConnection = aMode;
	}

}