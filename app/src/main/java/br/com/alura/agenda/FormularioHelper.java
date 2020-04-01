package br.com.alura.agenda;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import br.com.alura.agenda.modelo.Aluno;

public class FormularioHelper {

    private final EditText campoNome;
    private final EditText campoEndereco;
    private final EditText campoTelefone;
    private final EditText campoSite;
    private final RatingBar campoNota;
    private final ImageView campoFoto;

    private Aluno aluno;

    public FormularioHelper(FormularioActivity activity) {
        campoNome = (EditText) activity.findViewById(R.id.formulario_nome);
        campoEndereco = (EditText) activity.findViewById(R.id.formulario_endereco);
        campoTelefone = (EditText) activity.findViewById(R.id.formulario_telefone);
        campoSite = (EditText) activity.findViewById(R.id.formulario_site);
        campoNota = (RatingBar) activity.findViewById(R.id.formulario_nota);
        campoFoto = activity.findViewById(R.id.formulario_foto);
        aluno = new Aluno();
    }

    public Aluno getAluno() {

        aluno.setNome(campoNome.getText().toString());
        aluno.setEndereco(campoEndereco.getText().toString());
        aluno.setTelefone(campoTelefone.getText().toString());
        aluno.setSite(campoSite.getText().toString());
        aluno.setNota(Double.valueOf(campoNota.getProgress()));
        aluno.setCaminhoFoto((String) campoFoto.getTag()); // recuperando o caminho com o atributo TAG

        return aluno;
    }

    public void preencheFormulario(Aluno aluno) {
        campoNome.setText(aluno.getNome());
        campoEndereco.setText(aluno.getEndereco());
        campoTelefone.setText(aluno.getTelefone());
        campoSite.setText(aluno.getSite());
        campoNota.setProgress(aluno.getNota().intValue());
        carregaImagem(aluno.getCaminhoFoto());
        this.aluno = aluno;
    }

    /*
       No Android 7

       Se caso você esteja usando a versão 7 do android, a operação de chamar a câmera muda um
       pouco, a partir dela não podemos mais fornecer um caminho para um arquivo dentro da
       área do nosso aplicativo para que outros aplicativos escrevam nesse local.

       Isso era o que estávamos fazendo quando passávamos o caminho da foto para que a câmera do Android
       tirasse a foto e salvasse no caminho que especificamos. Para fazer isso agora, precisamos fazer
       um processo mais burocrático para permitir que outros aplicativos escrevam na área do
       nosso aplicativo.

       O primeiro passo é alterar o nosso AndroidManifest.xml e registrar um Content Provider que vai ficar
       responsável por controlar o acesso de conteúdos de nosso aplicativo por parte de aplicativos externos:

        <application>
            <provider
                android:name="android.support.v4.content.FileProvider"
                android:authorities="${applicationId}.provider"
                android:exported="false"
                android:grantUriPermissions="true">
                <meta-data
                    android:name="android.support.FILE_PROVIDER_PATHS"
                    android:resource="@xml/provider_paths" />
            </provider>
        </application>

        Perceba que fazemos referência a um @xml/provider_paths que ainda não temos no nosso projeto.
        Precisamos então criar esse xml na pasta res/xml. Nesse arquivo, definiremos quais os caminhos
        e tipos de arquivo os quais daremos permissão de acesso para os outros aplicativos.
        Como não queremos ter um controle fino desse tipo de permissão, vamos apenas falar que o
        qualquer arquivo do diretório raiz da nossa aplicação pode ser acessado:

        <?xml version="1.0" encoding="utf-8"?>
        <paths xmlns:android="http://schemas.android.com/apk/res/android">
            <external-path name="external_files" path="." />
        </paths>

        Agora precisamos alterar o nosso código para passar o caminho da foto para a câmera usando o
        nosso FileProvider. A única parte que vai se modificar é no momento que precisarmos fazer o
        putExtra na Intent que vai chamar a câmera:

        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", arquivoFoto));
    */

    public void carregaImagem(String caminhoFoto) {

        if (caminhoFoto != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto); // usando o caminho da foto para decodifica-lo em bitmap

            /*
            Reduzindo qualidade da imagem
            createScaledBitmap(bitmap_a_reduzir, dimensao_width, dimensao_height, passar_filtro_para_melhorar_imagem)
             */
            Bitmap bitmapReduzido = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            campoFoto.setImageBitmap(bitmapReduzido); // inserindo no ImageView o bitmap reduzido
            campoFoto.setScaleType(ImageView.ScaleType.FIT_XY); // preenchendo totalmente o espaço da ImageView
            campoFoto.setTag(caminhoFoto); // inserindo em TAG o caminho da foto
        }

    }
}
