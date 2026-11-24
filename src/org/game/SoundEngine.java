package org.game;
 
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
 
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
 
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
 
public class SoundEngine {
 
    public static class Sound extends Resource {
 
        private int buffer;
        private int source;
 
        public Sound(File file) throws Exception {
            try (AudioInputStream original = AudioSystem.getAudioInputStream(file)) {
                AudioFormat src = original.getFormat();
 
                int channels = src.getChannels();
                if (channels != 1 && channels != 2) {
                    throw new IllegalArgumentException(
                            "OpenAL only supports mono or stereo sounds, got " + channels + " channels: " + file);
                }
 
                AudioFormat target = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        src.getSampleRate(),
                        16,
                        channels,
                        channels * 2,
                        src.getSampleRate(),
                        false); 
 
                byte[] data;
                try (AudioInputStream converted = AudioSystem.getAudioInputStream(target, original)) {
                    data = readAll(converted);
                }

                ByteBuffer pcm = ByteBuffer.allocateDirect(data.length);
                pcm.put(data);
                pcm.flip();
 
                int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
 
                buffer = alGenBuffers();
                source = alGenSources();
                try {
                    alBufferData(buffer, format, pcm, (int) target.getSampleRate());
                    alSourcei(source, AL_BUFFER, buffer);
                } catch (RuntimeException e) {
                    alDeleteSources(source);
                    alDeleteBuffers(buffer);
                    throw e;
                }
            }
        }
 
        private static byte[] readAll(AudioInputStream in) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) != -1) {
                out.write(chunk, 0, n);
            }
            return out.toByteArray();
        }
 
        public float getVolume() {
            return alGetSourcef(source, AL_GAIN);
        }
 
        public void setVolume(float volume) {
            alSourcef(source, AL_GAIN, Math.max(0f, volume));
        }
 
        public void play(boolean looping) {
            alSourcei(source, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
            alSourcePlay(source); 
        }
 
        public void stop() {
            alSourceStop(source);
        }
 
        @Override
        public void destroy() {
            alSourceStop(source);
            alSourcei(source, AL_BUFFER, 0); 
            alDeleteSources(source);
            alDeleteBuffers(buffer);
            super.destroy();
        }
    }
 
    private long device;
    private long context;
    private ALCCapabilities deviceCaps;
 
    public SoundEngine() throws Exception {
        device = alcOpenDevice((ByteBuffer) null); 
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device");
        }
 
        deviceCaps = ALC.createCapabilities(device);
 
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            alcCloseDevice(device);
            device = NULL;
            throw new IllegalStateException("Failed to create OpenAL context");
        }
 
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
    }
 
    public void destroy() throws Exception {
        alcMakeContextCurrent(NULL);
        if (context != NULL) {
            alcDestroyContext(context);
            context = NULL;
        }
        if (device != NULL) {
            alcCloseDevice(device);
            device = NULL;
        }
        AL.setCurrentProcess(null);
    }
}
