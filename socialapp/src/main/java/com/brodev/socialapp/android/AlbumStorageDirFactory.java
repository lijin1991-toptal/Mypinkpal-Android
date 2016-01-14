package com.brodev.socialapp.android;

import java.io.File;

public abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}
