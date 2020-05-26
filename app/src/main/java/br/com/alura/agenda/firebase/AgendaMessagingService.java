package br.com.alura.agenda.firebase;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.event.AtualizaListaAlunoEvent;
import br.com.alura.agenda.modelo.dto.AlunosSync;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import br.com.alura.agenda.sync.AlunosSincronizador;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * O primeiro passo é criar um projeto dentro do firebase para que possamos utilizar
 * o cloud messaging. Sendo assim, precisamos fazer login em nossa conta do Google e
 * depois ir até a página do firebase em https://console.firebase.google.com/ e
 * clicar Criar novo projeto.
 *
 * ATENÇÃO: é necessário acessar a área de configurações do FIREBASE, copiar a chave
 * autorizadora na aba Cloud Messaging e colar na aplicação WEB.
 * Como a aplicação WEB se comunicará com o servidor quando um novo aluno for cadastrado,
 * é necessário que o app WEB tenha a chave válida para se comunicar com o Firebase.
 *
 * Push Notification: servidor envia requisições para o cliente.
 *
 * Firebase: é uma plataforma do Google que oferece diversos serviços,
 * entre eles o Cloud Messaging.
 *
 * Para baixar o servidor em ZIP:
 * https://github.com/alura-cursos/android-sync-parte-1/archive/servidor.zip
 *
 * Para acessar o servidor no GITHUB:
 * https://github.com/alura-cursos/android-sync-parte-1
 *
 */
public class AgendaMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        Existem muitas possibilidades para o tratamento da mensagem, mas o
        nosso servidor envia as informações por meio do data que é um Map
        de chave e valor.
         */
        Map<String, String> mensagem = remoteMessage.getData();
        Log.i("firebase_log", String.valueOf(mensagem));

        converteParaAluno(mensagem);
    }

    /*
    Com o json em mãos o que precisamos agora é de fato realizar a conversão para o
    objeto da nossa App.
    Atualmente o Retrofit faz manualmente esse processo de conversão, ou seja, o único método
    que conhecemos até o momento seria por meio da classe JSONStringer que era aquele método
    um pouco trabalhoso... Entretanto, já que estamos usando o Jackson como conversor do
    Retrofit, podemos também utilizá-lo para converter essa String!
     */
    private void converteParaAluno(Map<String, String> mensagem) {

        String chaveDeAcesso = "alunoSync";

        if(mensagem.containsKey(chaveDeAcesso)){

            String json = mensagem.get(chaveDeAcesso);
            ObjectMapper mapper = new ObjectMapper();

            try {
                // Convertendo o JSON em uma instância de AlunosSync
                AlunosSync alunoSync = mapper.readValue(json, AlunosSync.class);

                /*
                Pode parecer estranho usar o "this" como contexto para o AlunoDAO já que não
                estamos em uma Activity.
                No entanto, por baixo dos panos, a classe FirebaseMessagingService tem uma
                extensão de Context, tornando isso possível.
                 */
                //AlunoDAO alunoDAO = new AlunoDAO(this);
                //alunoDAO.sincroniza(alunoSync.getAlunos());
                //alunoDAO.close();
                new AlunosSincronizador(this).sincroniza(alunoSync);

                /*
                ATUALIZANDO O ADAPTER DA ACTIVITY

                A solução que utilizaremos funciona da seguinte forma: uma notificação é
                enviada e a listagem quando estiver em foco, fará a atualização de si mesma.

                Desta forma, garantimos a atualização da listagem só quando ela estiver em uso.
                A forma de fazer isso é por meio da biblioteca EventBus.

                A biblioteca EventBus usa o conceito de publish/subscribe no qual o emissor do evento é
                o publisher e o ouvinte/receptor do evento é o subscriber. Desta forma, o emissor
                envia um evento para o EventBus e o mesmo se encarrega de distribuir os eventos para
                as outras entidades que são subscriber daquele evento.

                NOTA: EventBus é uma biblioteca que deve ser adicionada ao projeto via gradle.
                 */

                /*
                Essa notificação/evento deve ser uma classe criada por nós mesmos, que indicará
                qual exatamente foi o evento. Esta classe também servirá para indicar para o
                EventBus quais outras entidades serão notificadas. Isso porque estas outras
                entidades serão subscriber desta classe de evento. Chamaremos a nova classe
                de AtualizaListaAlunoEvent para indicar exatamente o que ela significa.
                 */
                EventBus eventBus = EventBus.getDefault();
                eventBus.post(new AtualizaListaAlunoEvent());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * O método onTokenRefresh só será chamado uma vez no ciclo de vida da aplicação,
     * porque não faz sentido gerar token com frequência. Para que consigamos gerar um novo,
     * temos algumas opções: desinstalar a aplicação, limpar os dados da mesma ou instalar
     * ela em outro aparelho.
     */
    @Override
    public void onNewToken(String token) {
        Log.d("firebase_log", "Refreshed token: " + token);

        /*
        Esse token é importante porque é por meio dele que o firebase enviará as
        informações vindas do servidor.
         */
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Agora que o nosso projeto contém todas as configurações e dependências necessárias
     * para utilizarmos o FCM, precisamos realizar o primeiro passo que é justamente gerar o
     * token identificador, pois dessa forma o FCM criará o vínculo (conexão) com o
     * dispositivo para que seja possível enviar as mensagens!
     *
     * @param token
     */
    private void sendRegistrationToServer(String token) {
        Call<Void> call = new RetrofitInicializador().getDispositivoService().enviaToken(token);

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.i("token enviado", token);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }
}
