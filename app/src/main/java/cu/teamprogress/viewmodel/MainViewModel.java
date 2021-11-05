package cu.teamprogress.viewmodel;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private String linkTM;

    public MainViewModel() {
        this.linkTM = "";
    }

    public String getLinkTM() {
        return linkTM;
    }

    public void setLinkTM(String linkTM) {
        this.linkTM = linkTM;
    }

    public String fixLinkTM(String linkTM){
        StringBuilder linkBuffer = new StringBuilder(linkTM);

        for(int i = linkBuffer.length() -1; i>= 0 ;i--){
            if (linkBuffer.charAt(i) == ','){
                linkBuffer.setCharAt(i,'.');
                break;
            }
        }
        return linkBuffer.toString();

    }
}
