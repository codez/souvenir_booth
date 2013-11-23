
package ch.codez.souvenirbooth.controller;

import ch.codez.souvenirbooth.model.SouvenirImage;

public interface DirectorListener {
    
    public void countDownAt(int i);
    
    public void processing();
    
    public void ready(SouvenirImage image);
    
}
