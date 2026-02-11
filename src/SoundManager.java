import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
public class SoundManager {
    Clip clip;
    public void playEffect(URL file){
        //System.out.println(file);
        if(clip != null && clip.isRunning()) clip.stop();
        try {
            //URL url = getClass().getResource("src/res/"+file);

            if(file == null) return;

            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();

        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

    }
    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
