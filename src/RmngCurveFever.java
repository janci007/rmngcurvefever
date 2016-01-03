/**
 * Rouming curve fever notification service
 * @author: janci
 * 
 */

import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class RmngCurveFever {
    
    // number of seconds between forum checks
    private final static int checkInterval = 10; 
    private final String forumUrl = "http://www.rouming.cz/roumingForum.php";
    private final Pattern feverRegex = Pattern.compile("\"(.*curvefever\\.com[^\"]*)");
    private final Pattern dateRegex = Pattern.compile("\\(([0-9]{1,2})\\.([0-9]{1,2}).([0-9]{4}) ([0-9]{1,2}):([0-9]{1,2})\\)");

    private Game lastGame = null;
    
    private class Game{
	public Date date;
	public String url;
	Game(Date date, String url){
	    this.date = date;
	    this.url = url;
	}
    }
    
    
    
    public static void main(String[] args) {
	final RmngCurveFever f = new RmngCurveFever();
	try {
	    TrayIcon icon = new TrayIcon(ImageIO.read(f.getClass().getResource("/icon.png")), "Rouming Curve Fever notification service");
	    icon.setImage(icon.getImage().getScaledInstance((int) icon.getSize().getWidth(), -1, Image.SCALE_SMOOTH));
	    PopupMenu popup = new PopupMenu();
	    popup.add(new MenuItem("Exit"));
	    popup.addActionListener(new ActionListener() {	        
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            System.exit(0);
	        }
	    });
	    icon.setPopupMenu(popup);
	    SystemTray.getSystemTray().add(icon);	    
	} catch (Exception e) {
	    return;
	}
	new java.util.Timer().schedule(new TimerTask() {
	    @Override
	    public void run() {
		f.checkGame();
	    }
	}, 0, checkInterval*1000);
	
    }
	
    public void checkGame(){
	Game g = findGame();
	if(g != null){
	    if(isNewGame(g)){
		showNotif(g);
	    }
	    lastGame = g;
	}
    }
    
    private void showNotif(final Game g){
	final JFrame notif = new JFrame("New game!!!");
	notif.setLayout(null);
	notif.setSize(591, 499);
	try {
	    notif.setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
	} catch (IOException e1) {}
	JLabel img = new JLabel(new ImageIcon(getClass().getResource("/notif.png")));
	img.setBounds(0, 0, 591, 499);
	notif.add(img);
	JButton btn = new JButton("Play!");
	btn.setBounds(220, 365, 150, 50);
	btn.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		notif.setVisible(false);
		notif.dispose();
		 try {
		     Desktop.getDesktop().browse(new URL(g.url).toURI());
		 } catch (Exception e) {
		     Runtime runtime = Runtime.getRuntime();
		     try {
			 runtime.exec("xdg-open "+ g.url);
		     } catch (Exception ee) {
			 JOptionPane.showInputDialog("Unable to open browser", g.url);
		     }
		 }
	    }
	});
	notif.add(btn);
	notif.setVisible(true);
    }
    
    private boolean isNewGame(Game g){
	Date lastDate = new Date(System.currentTimeMillis() - 30*60*1000);
	if(lastGame != null){
	    lastDate = lastGame.date;
	}
	return g.date.getTime() > (lastDate.getTime() + 10000);
    }
    
    
    private Game findGame(){
        String body = httpGet(forumUrl);
        Matcher m = feverRegex.matcher(body);
        if(m.find()){
            String curveUrl = m.group(1);
            Matcher dateMatcher = dateRegex.matcher(body.substring(0, m.start()));
            Calendar c = Calendar.getInstance();
            while(dateMatcher.find()){       	
        	c.set( 
        		Integer.parseInt(dateMatcher.group(3)),
        		Integer.parseInt(dateMatcher.group(2))-1,
        		Integer.parseInt(dateMatcher.group(1)),
        		Integer.parseInt(dateMatcher.group(4)),
        		Integer.parseInt(dateMatcher.group(5)),
        		0
        	);
            }
            return new Game(c.getTime(), curveUrl);
        }
        return null;
    }
    
    private String httpGet(String sURL){
	StringBuilder response = new StringBuilder();
	try {
            URL url = new URL(sURL);
            InputStream is = url.openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;	
	    while((line = rd.readLine()) != null) {
	      response.append(line);
	      response.append('\n');
	    }
	    rd.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return response.toString();
    }
    
    

}
