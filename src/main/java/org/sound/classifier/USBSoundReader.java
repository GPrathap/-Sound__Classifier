package org.sound.classifier;

import javax.sound.sampled.*;

/**
 *
 * @author d07114915
 *
 * Class to get a mixer with a specified recordable audio format from a specified port
 * For instance get a 44.1kHz 16bit record line for a "line in"  input
 */
public class USBSoundReader {
    private static final String THE_INPUT_TYPE_I_WANT = "LINE_IN";
    private static final String THE_NAME_OF_THE_MIXER_I_WANT_TO_GET_THE_INPUT_FROM = "Port Device [hw:1]";
    private static final AudioFormat af = new AudioFormat(
            AudioFormat.Encoding.ALAW,
            44100.0F,
            16,
            2,
            2 * 2,
            44100.0F,
            false);
    private static final DataLine.Info targetDataLineInfo = new DataLine.Info(TargetDataLine.class, af);
    private static final Port.Info myInputType = new Port.Info((Port.class), THE_INPUT_TYPE_I_WANT, true);
    private static TargetDataLine targetDataLine = null;

    public static void main(String[] args) {
        Mixer portMixer = null;
        Mixer targetMixer = null;
        try {
            for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
                               System.out.println("-" +mi.getName() + "-");
                if (mi.getName().equals(THE_NAME_OF_THE_MIXER_I_WANT_TO_GET_THE_INPUT_FROM)) {
                    System.out.println("Trying to get portMixer for :" + mi.getName());
                    portMixer = getPortMixerInfoFor(mi);
                    if (portMixer != null) {
                        System.out.println(portMixer.getMixerInfo().toString());
                        targetMixer = AudioSystem.getMixer(mi);
                        break;
                    }
                }
            }
            if (targetMixer != null) {
                targetMixer.open();

                targetDataLine = (TargetDataLine) targetMixer.getLine(targetDataLineInfo);
                System.out.println("Got TargetDataLine from :" + targetMixer.getMixerInfo().getName());

                portMixer.open();

                Port port = (Port) portMixer.getLine(myInputType);
                port.open();

                Control[] controls = port.getControls();
                System.out.println((controls.length > 0 ? "Controls for the "+ THE_INPUT_TYPE_I_WANT + " port:" : "The port has no controls."));
                for (Control c : controls) {
                    System.out.println(c.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //return the portMixer that corresponds to TargetMixer
    private static Mixer getPortMixerInfoFor(Mixer.Info mixerInfo) {
        //Check this out for interest
        //http://www.java-forum.org/spiele-multimedia-programmierung/94699-java-sound-api-zuordnung-port-mixer-input-mixer.html
        try {
            // get the requested mixer
            Mixer targetMixer = AudioSystem.getMixer(mixerInfo);
            targetMixer.open();
            //Check if it supports the desired format
            if (!targetMixer.isLineSupported(targetDataLineInfo)) {
                System.out.println(mixerInfo.getName() + " supports recording @ " + af);
                //now go back and start again trying to match a mixer to a port
                //the only way I figured how is by matching name, because
                //the port mixer name is the same as the actual mixer with "Port " in front of it
                // there MUST be a better way
                for (Mixer.Info portMixerInfo : AudioSystem.getMixerInfo()) {
                    String port_string = "Port ";
                    if ((port_string + mixerInfo.getName()).equals(portMixerInfo.getName())) {
                        System.out.println("Matched Port to Mixer:" + mixerInfo.getName());
                        Mixer portMixer = AudioSystem.getMixer(portMixerInfo);
                        portMixer.open();
                        //now check the mixer has the right input type eg LINE_IN
                        boolean lineTypeSupported = portMixer.isLineSupported((Line.Info) myInputType);
                        System.out.println(portMixerInfo.getName() +" does " + (lineTypeSupported? "" : "NOT") + " support " + myInputType.getName());
                        if (lineTypeSupported) {
                            portMixer.close();
                            targetMixer.close();
                            return portMixer;
                        }
                        portMixer.close();
                    }
                }
            }
            targetMixer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}