package sm.client;

import sm.client.R;
import sm.client.SMClientActivity;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class SMClientActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client);
        
        String imagefile ="/sdcard/DSC01576.JPG";
        ImageView image = (ImageView)findViewById(R.id.imagem_video);
        Bitmap bm = BitmapFactory.decodeFile(imagefile);
        image.setImageBitmap(bm);
        
        
        // ação do botao de setup
        final Button botaoSetup = (Button) findViewById(R.id.botao_setup);
        botaoSetup.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
				final Dialog dialog = new Dialog(SMClientActivity.this);
				dialog.setContentView(R.layout.setup);
				dialog.setTitle("Configurar");
				
				Button botaoCancelar = (Button) dialog.findViewById(R.id.botao_cancelar);
				botaoCancelar.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.cancel();
						
					}
				});
				
				dialog.show();
			}
		});
        
        
    }
}