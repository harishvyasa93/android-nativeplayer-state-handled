package com.harish.player.wrapper;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * @author HARISH.
 *         <p>
 *         Wraps the native {@link android.media.MediaPlayer} class.
 *         This class provides the current state of the player whether it is in preparing, buffering, idle, etc.
 * @see android.media.MediaPlayer for more information.
 * @since 18.07.2018
 */
public final class Player implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener {
    private static final String TAG = Player.class.getSimpleName();

    //States.
    public static final int STATE_UNKNOWN = -2;     //Before player object is created.
    public static final int STATE_ERROR = -1;       //When playback resulted in error.
    public static final int STATE_IDLE = 0;         //Not initialized, but player object is created.
    public static final int STATE_INITIALIZED = 1;  //Initialized with a data source.
    public static final int STATE_PREPARING = 2;    //Preparing resources for playback.
    public static final int STATE_PREPARED = 3;     //Player is ready for callback, waiting for #start() call.
    public static final int STATE_STARTED = 4;      //Immediately after calling #start().
    public static final int STATE_PAUSED = 5;       //Paused temporarily, ready to be resumed.
    public static final int STATE_STOPPED = 6;      //Stopped, calling #start() will start from beginning.
    public static final int STATE_COMPLETED = 7;    //Playback is completed, or if onError() not registered might have resulted in STATE_COMPLETED.
    public static final int STATE_ENDED = 8;        //After calling #release(), all resources are released and state ends here.

    //Native Player instance.
    private MediaPlayer mPlayer;

    //Current player state.
    private int mCurrentState = STATE_UNKNOWN;

    //Listeners.
    private PlayerPreparationListener mPreparedListener;
    private PlayerBufferingUpdateListener mBufferingUpdateListener;
    private PlaybackCompletionListener mCompletionListener;
    private PlayerSeekCompletionListener mSeekCompletionListener;
    private PlayerInfoListener mInfoListener;
    private PlayerErrorListener mErrorListener;

    /**
     * Constructor.
     */
    public Player() {
        mPlayer = new MediaPlayer();
        mCurrentState = STATE_IDLE;
    }

    /**
     * Returns the current state of the player.
     *
     * @return one of the states like
     * {@link #STATE_IDLE}
     * {@link #STATE_INITIALIZED}
     * {@link #STATE_PREPARING}
     * {@link #STATE_PREPARED}
     * {@link #STATE_STARTED}
     * {@link #STATE_PAUSED}
     * {@link #STATE_STOPPED}
     * {@link #STATE_COMPLETED}
     * {@link #STATE_ENDED}
     * {@link #STATE_ERROR}
     * {@link #STATE_UNKNOWN}
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    /**
     * Sets the current state.
     *
     * @param mCurrentState The current player state.
     */
    private void setCurrentState(int mCurrentState) {
        this.mCurrentState = mCurrentState;
    }

    /**
     * Checks whether the MediaPlayer is playing.
     *
     * @return true if currently playing, false otherwise
     * @throws IllegalStateException if the internal player engine has not been
     *                               initialized or has been released.
     */
    public boolean isPlaying() throws IllegalStateException {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        throw new IllegalStateException("Player engine not initialized or has been released!");
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     *                               <p>
     *                               <p>When <code>path</code> refers to a local file, the file may actually be opened by a
     *                               process other than the calling application.  This implies that the pathname
     *                               should be an absolute path (as any other process runs with unspecified current working
     *                               directory), and that the pathname should reference a world-readable file.
     *                               As an alternative, the application could first open the file for reading,
     *                               and then use the file descriptor form.
     */
    public synchronized void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(path);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(fd);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public synchronized void setDataSource(@NonNull AssetFileDescriptor afd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(afd);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri     the Content URI of the data you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void setDataSource(@NonNull Context context, @NonNull Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(context, uri);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source as a {@link MediaDataSource} instance.
     *
     * @param dataSource The {@link MediaDataSource} containing the URI of data to play.
     * @throws IllegalArgumentException if the player couldn't resolve the arguments.
     * @throws IllegalStateException    if it is called in an invalid state
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public synchronized void setDataSource(MediaDataSource dataSource)
            throws IllegalArgumentException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(dataSource);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri     the Content URI of the data you want to play
     * @param headers the headers to be sent together with the request for the data
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void setDataSource(@NonNull Context context, @NonNull Uri uri,
                                           @Nullable Map<String, String> headers)
            throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(context, uri, headers);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd     the FileDescriptor for the file you want to play
     * @param offset the offset into the file where the data to be played starts, in bytes
     * @param length the length in bytes of the data to be played
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException {
        if (mPlayer != null) {
            mPlayer.setDataSource(fd, offset, length);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri     the Content URI of the data you want to play
     * @param headers the headers to be sent together with the request for the data
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     * @throws IllegalStateException if it is called in an invalid state
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public synchronized void setDataSource(@NonNull Context context, @NonNull Uri uri,
                                           @Nullable Map<String, String> headers,
                                           @Nullable List<HttpCookie> cookies)
            throws IOException {
        if (mPlayer != null) {
            mPlayer.setDataSource(context, uri, headers, cookies);
            setCurrentState(STATE_INITIALIZED);
        }
    }

    /**
     * Prepares the player for playback, synchronously.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void prepare() throws IOException, IllegalStateException {
        if (mPlayer != null) {
            //Waiting call, hence setting state before calling prepare() on the parent class.
            setCurrentState(STATE_PREPARING);
            mPlayer.prepare();
        }
    }

    /**
     * Prepares the player for playback, asynchronously.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void prepareAsync() throws IllegalStateException {
        if (mPlayer != null) {
            mPlayer.prepareAsync();
            setCurrentState(STATE_PREPARING);
        }
    }

    /**
     * Starts or resumes playback. If playback had previously been paused,
     * playback will continue from where it was paused. If playback had
     * been stopped, or never started before, playback will start at the
     * beginning.
     *
     * @throws IllegalStateException if it is called in an invalid state
     */
    public synchronized void start() throws IllegalStateException {
        if (mPlayer != null) {
            mPlayer.start();
            setCurrentState(STATE_STARTED);
        }
    }

    /**
     * Stops playback after playback has been stopped or paused.
     *
     * @throws IllegalStateException if the internal player engine has not been
     *                               initialized.
     */
    public synchronized void stop() throws IllegalStateException {
        if (mPlayer != null) {
            mPlayer.stop();
            setCurrentState(STATE_STOPPED);
        }
    }

    /**
     * Pauses playback. Call start() to resume.
     *
     * @throws IllegalStateException if the internal player engine has not been
     *                               initialized.
     */
    public synchronized void pause() throws IllegalStateException {
        if (mPlayer != null) {
            mPlayer.pause();
            setCurrentState(STATE_PAUSED);
        }
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     * It is considered good practice to call this method when you're
     * done using the MediaPlayer. In particular, whenever an Activity
     * of an application is paused (its onPause() method is called),
     * or stopped (its onStop() method is called), this method should be
     * invoked to release the MediaPlayer object, unless the application
     * has a special need to keep the object around. In addition to
     * unnecessary resources (such as memory and instances of codecs)
     * being held, failure to call this method immediately if a
     * MediaPlayer object is no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback
     * failure for other applications if no multiple instances of the
     * same codec are supported on a device. Even if multiple instances
     * of the same codec are supported, some performance degradation
     * may be expected when unnecessary multiple instances are used
     * at the same time.
     */
    public synchronized void release() {
        if (mPlayer != null) {
            mPlayer.release();
            setCurrentState(STATE_ENDED);
        }
    }

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling
     * this method, you will have to initialize it again by setting the
     * data source and calling prepare().
     */
    public synchronized void reset() {
        if (mPlayer != null) {
            mPlayer.reset();
            setCurrentState(STATE_IDLE);
        }
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public synchronized void setOnPreparedListener(PlayerPreparationListener listener) {
        if (mPlayer != null) {
            this.mPreparedListener = listener;
            mPlayer.setOnPreparedListener(this);
        }
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public synchronized void setOnCompletionListener(PlaybackCompletionListener listener) {
        if (mPlayer != null) {
            this.mCompletionListener = listener;
            mPlayer.setOnCompletionListener(this);
        }
    }

    /**
     * Register a callback to be invoked when the status of a network
     * stream's buffer has changed.
     *
     * @param listener the callback that will be run.
     */
    public synchronized void setOnBufferingUpdateListener(PlayerBufferingUpdateListener listener) {
        if (mPlayer != null) {
            this.mBufferingUpdateListener = listener;
            mPlayer.setOnBufferingUpdateListener(this);
        }
    }

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    public synchronized void setOnSeekCompleteListener(PlayerSeekCompletionListener listener) {
        if (mPlayer != null) {
            this.mSeekCompletionListener = listener;
            mPlayer.setOnSeekCompleteListener(this);
        }
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public synchronized void setOnErrorListener(PlayerErrorListener listener) {
        if (mPlayer != null) {
            this.mErrorListener = listener;
            mPlayer.setOnErrorListener(this);
        }
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener the callback that will be run
     */
    public synchronized void setOnInfoListener(PlayerInfoListener listener) {
        if (mPlayer != null) {
            this.mInfoListener = listener;
            mPlayer.setOnInfoListener(this);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.mPlayer == mediaPlayer) {
            setCurrentState(STATE_COMPLETED);
            //Pass the playback completion state.
            if (mCompletionListener != null) {
                mCompletionListener.onPlaybackCompleted();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (this.mPlayer == mediaPlayer) {
            //Pass the error information.
            setCurrentState(STATE_ERROR);
            if (mErrorListener != null) {
                return mErrorListener.onError(what, extra);
            }
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        if (this.mPlayer == mediaPlayer) {
            //Pass the information received.
            if (mInfoListener != null) {
                return mInfoListener.onInfo(what, extra);
            }
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (this.mPlayer == mediaPlayer) {
            setCurrentState(STATE_PREPARED);
            //Broadcast preparation status.
            if (mPreparedListener != null) {
                mPreparedListener.onPlayerPrepared();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        if (this.mPlayer == mediaPlayer) {
            //Broadcast buffering update info.
            if (mBufferingUpdateListener != null) {
                mBufferingUpdateListener.onBufferingUpdate(percent);
            }
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        if (this.mPlayer == mediaPlayer) {
            //Pass the seek completion status.
            if (mSeekCompletionListener != null) {
                mSeekCompletionListener.onSeekCompleted();
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface PlayerPreparationListener {
        void onPlayerPrepared();
    }

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    public interface PlayerBufferingUpdateListener {
        void onBufferingUpdate(int percent);
    }

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface PlaybackCompletionListener {
        void onPlaybackCompleted();
    }

    /**
     * Interface definition of a callback to be invoked to communicate some
     * info and/or warning about the media or its playback.
     */
    public interface PlayerInfoListener {
        boolean onInfo(int what, int extra);
    }

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation.
     */
    public interface PlayerErrorListener {
        boolean onError(int what, int extra);
    }

    /**
     * Interface definition of a callback to be invoked indicating
     * the completion of a seek operation.
     */
    public interface PlayerSeekCompletionListener {
        void onSeekCompleted();
    }
}
