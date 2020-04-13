package br.com.alura.agenda;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;

public class FormularioActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 200;
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        helper = new FormularioHelper(this);

        Intent intent = getIntent();
        Aluno aluno = (Aluno) intent.getSerializableExtra("aluno");

        if(aluno != null){
            helper.preencheFormulario(aluno);
        }

        Button botaoFoto = findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                Não é o caminho certo para resolver isso, isso basicamente removerá as políticas de
                modo estrito  e ignorará o aviso de segurança.

                 */
                //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                //StrictMode.setVmPolicy(builder.build());

                /*
                INVOCANDO AÇÃO DE TIRAR UMA FOTO.

                CAMINHO ONDE QUEREMOS SALVAR A FOTO MAIS O NOME DO ARQUIVO.
                getExternalFilesDir(null) = PEGANDO A PASTA EDITÁVEL DA NOSSA APLICAÇÃO.
                O VALOR null SIGNIFICA QUE IREMOS UTILIZAR O DIRETÓRIO RAIZ DA APLICAÇÃO.
                SE QUISÉSSEMOS, PODERÍAMOS UTILIZAR ALGUMAS VARIÁVEIS QUE O PRÓPRIO ANDROID
                DISPONIBILIZA PARA PODERMOS SALVAR EM DIRETÓRIOS MAIS ESPECÍFICOS.

                MediaStore.EXTRA_OUTPUT = NESSA LINHA IREMOS INFORMAR QUE SALVAREMOS A FOTO
                NO LOCAL QUE DESEJAMOS.
                 */
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                caminhoFoto = getExternalFilesDir("fotos") + "/"+ System.currentTimeMillis() +".jpg";

                Log.i("caminhofoto", "caminho: " + caminhoFoto);
                File arquivoFoto = new File(caminhoFoto);

                //intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(FormularioActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider", arquivoFoto));
                startActivityForResult(intentCamera, REQUEST_CODE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_formulario_ok:
                Aluno aluno = helper.getAluno();
                AlunoDAO dao = new AlunoDAO(this);

                if(aluno.getId() != null){
                    dao.altera(aluno);
                }else{
                    dao.insere(aluno);
                }


                dao.close();
                Toast.makeText(FormularioActivity.this, "Aluno " + aluno.getNome() + " salvo!", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            helper.carregaImagem(caminhoFoto);
        }
    }
}
