package br.com.alura.agenda.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import br.com.alura.agenda.R;
import br.com.alura.agenda.dao.AlunoDAO;

/**
 * Quando o Android recebe um SMS, ele irá querer verificar a quais aplicativos interessa esta
 * mensagem. Assim que ele identifica, é feito uma cópia do SMS para os aplicativos que irão tratá-lo.
 * Isto significa que o Android "baterá na porta" de todos os apps - esta operação nós chamamos de broadcast.
 * Porém, os aplicativos terão que manifestar esse interesse, é o que a nossa agenda precisará fazer também.
 *
 * Vamos criar uma classe que tratará o recebimento deste SMS e incluir, por enquanto, um Toast.
 * Vamos criar um novo pacote chamado receiver e dentro dele, vamos adicionar a classe SMSReceiver
 * e vamos estender uma classe do Android que já existe BroadcastReceiver.
 * Quando chegar o evento, o Android irá chamar o método onReceive.
 *
 * Nós criamos uma classe, que o Android não sabe ainda que existe.
 *
 * No Toast, vamos uma usar o context e a nossa aplicação irá exibir a mensagem Chegou um SMS!.
 * Usamos o LENGTH_SHORT, porque a duração será curto, e invocamos o método show, para o nosso
 * Toast não ficar guardado.
 *
 * Em seguida, iremos no arquivo manifest e dentro dela iremos incluir nosso Receiver e qual evento
 * desejamos evento estaremos monitorando, no caso, é o de SMS:
 * <receiver android:name=".receiver.SMSReceiver">
 *      <intent-filter>
 *          <action android:name="android.provider.Telephony.SMS_RECEIVED" />
 *      </intent-filter>
 * </receiver>
 *
 * Por questões de segurança, o Android pede que toda aplicação que roda com SMS trabalhe com uma
 * permissão especifica. Iremos trabalhar com a tag uses-permission, posicionada na parte de
 * cima do manifest.
 *
 * <uses-permission android:name="android.permission.RECEIVE_SMS"/>
 */
public class SMSReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        O ideal aqui seria pedir a permissão ao usuário caso ele ainda não tenha permitido.
        Contudo, para evitar de fazer o mesmo código que fizemos em ListaAlunoActivity quanto ao
        uso da ligação, nós liberamos o acesso direto pelo celular.
        Essa solicitação de permissão deve ser inserida na Activity principal, no método onCreate().
         */

        /*
        Para que um SMS consiga trafegar entre celulares mais novos e antigos, as mensagens
        possuem um formato específico, chamado de PDU (protocol data unit).

        Então, teremos que pegar a pdu dentro da Intent, convertê-lo para um SmsMessage.
        Iremos adicionar o método createFromPdu() e passaremos dois parâmetros: pdu e
        formato. Ambos virão da Intent.

        Após importarmos a classe SmsMessage, precisaremos desempacotar o pdu da Intent.
        Usaremos o método getSerializableExtra, que irá nos pedir a chave do que queremos recuperar.
        A documentação do Android irá nos informar que a chave é pdus. Iremos guardá-lo em um array
        de objects. Cada pdu será um Object.

        Quando escrevemos um SMS, cada mensagem deve conter 160 caracteres. Em alguns celulares,
        quando escrevemos uma quantidade superior a essa, a mensagem será quebrada em duas mensagens
        ou mais - cada uma terá um pdu. E por isso, teremos um array com vários pdus.
         */
        Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

        /*
        No cabeçalho de cada pdu estará um número de telefone. Se não for uma uma "multimensagem",
        podemos aproveitar o pdu do primeiro SMS no array, ou seja, aquela que ocupar a posição 0.
         */
        byte[] pdu = (byte[]) pdus[0];
        String formato = (String) intent.getSerializableExtra("format");

        SmsMessage sms = SmsMessage.createFromPdu(pdu, formato);

        String telefone = sms.getDisplayOriginatingAddress(); // pegando o número que enviou a mensagem

        Log.i("isaluno", "telefone: " + telefone);
        // procurando pelo aluno na base de dados
        AlunoDAO dao = new AlunoDAO(context);
        if(dao.isAluno(telefone)) {
            Log.i("isaluno", "é aluno");
            Toast.makeText(context, "Chegou um SMS!", Toast.LENGTH_SHORT).show();

            // executando um som quando receber a mensagem de algum aluno conhecido
            MediaPlayer mp = MediaPlayer.create(context, R.raw.msg);
            mp.start();
        }
        Log.i("isaluno", "não é aluno");
        dao.close();
    }
}
