# Documentação Técnica — Aplicativo Wear OS
**Interação com Sensores de Smartphones e Wearables**

---

## 1. Visão Geral do Projeto

Este aplicativo foi desenvolvido para a plataforma Wear OS com o objetivo de proporcionar comunicação eficaz e assistência para funcionários com necessidades especiais da empresa fictícia "Doma". O sistema detecta saídas de áudio disponíveis no dispositivo e reproduz mensagens em voz alta, respondendo de forma adaptativa ao hardware encontrado.

O projeto cobre os requisitos da missão acadêmica sobre implementação de saídas de áudio em Wear OS, detecção dinâmica de dispositivos e facilitação da conexão Bluetooth.

---

## 2. Arquitetura do Projeto

O projeto é composto pelas seguintes classes e arquivos:

- **MainActivity.java** — Ponto de entrada do app. Gerencia TTS, MediaPlayer, callbacks de áudio e UI.
- **AudioHelper.java** — Verifica disponibilidade de dispositivos de áudio (speaker embutido, Bluetooth A2DP).
- **activity_main.xml** — Layout simples com TextView para exibir o status atual na tela do relógio.
- **AndroidManifest.xml** — Declara permissões e define o app como standalone Wear OS.

---

## 3. Implementações Solicitadas pelo Professor

### 3.1 Verificação de Saídas de Áudio

Foi implementada a classe `AudioHelper` com o método `audioOutputAvailable()`, que utiliza `AudioManager.getDevices()` para verificar:

- `AudioDeviceInfo.TYPE_BUILTIN_SPEAKER` — alto-falante embutido do relógio
- `AudioDeviceInfo.TYPE_BLUETOOTH_A2DP` — fone de ouvido Bluetooth pareado

### 3.2 Detecção Dinâmica de Dispositivos

Foi registrado um `AudioDeviceCallback` via `audioManager.registerAudioDeviceCallback()` para detectar em tempo real:

- `onAudioDevicesAdded` — quando um fone Bluetooth é conectado
- `onAudioDevicesRemoved` — quando um fone Bluetooth é desconectado

### 3.3 Facilitação da Conexão Bluetooth

Quando nenhuma saída de áudio está disponível, o app direciona o usuário às configurações Bluetooth:

```java
Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
```

### 3.4 Reprodução de Áudio

O app utiliza `TextToSpeech` para sintetizar mensagens em voz alta, com idioma configurado para `pt-BR` e fallback para `en-US` caso o idioma não esteja disponível.

---

## 4. Implementações Adicionais (Além do Solicitado)

Durante o desenvolvimento foram necessárias adaptações não previstas no enunciado original.

### 4.1 Fallback de MediaPlayer quando TTS não está disponível

**Problema:** o emulador Wear OS não possui motor TTS instalado por padrão, retornando `status: -1` na inicialização.

**Solução:** foi adicionada uma flag `usarTTS` que controla qual mecanismo utilizar. Se o TTS inicializar com sucesso, é usado normalmente. Caso contrário, o app recorre ao `MediaPlayer` para reproduzir um arquivo de áudio local (`res/raw/iniciado.mp3`).

### 4.2 Verificação do Alto-falante Embutido

**Problema:** o código original do enunciado verificava apenas Bluetooth, fazendo o app sempre abrir as configurações de Bluetooth mesmo em dispositivos com alto-falante integrado.

**Solução:** a lógica foi expandida para verificar primeiro o `TYPE_BUILTIN_SPEAKER`. Somente se não houver nenhuma saída disponível o app redireciona para as configurações Bluetooth.

### 4.3 Troca de AppCompatActivity por Activity

**Problema:** o uso de `AppCompatActivity` causava um crash fatal (`IllegalStateException`) porque o Wear OS não possui o tema `Theme.AppCompat` por padrão e não havia arquivo `themes.xml` no projeto.

**Solução:** a `MainActivity` passou a estender `android.app.Activity` diretamente, eliminando a dependência de tema AppCompat.

### 4.4 Ordem de Inicialização corrigida

**Problema:** o método `falar()` tentava atualizar o `TextView` antes de `setContentView()` ser chamado, causando `NullPointerException`.

**Solução:** `setContentView()` e `findViewById()` passaram a ser chamados antes de qualquer inicialização de TTS ou chamada a `falar()`.

### 4.5 Layout Visual para Wear OS

Não estava no enunciado, mas foi adicionado um layout básico (`activity_main.xml`) com um `TextView` centralizado em fundo preto, exibindo o status atual do sistema na tela do relógio.

### 4.6 Logs de Depuração

Foram adicionados `Log.d()` e `Log.e()` em pontos estratégicos para facilitar a depuração via Logcat, incluindo confirmação do tipo de saída detectada, status do TTS e mensagens sendo reproduzidas.

---

## 5. Fluxo de Execução do App

1. `setContentView()` carrega o layout e o `TextView` é referenciado.
2. `AudioManager` e `AudioHelper` são inicializados.
3. `TextToSpeech` tenta inicializar. Se bem-sucedido, `usarTTS = true`.
4. `verificarSaidaDeAudio()` é chamado: detecta speaker ou Bluetooth.
5. Se há saída de áudio → `falar("Sistema iniciado com sucesso")`.
6. Se não há saída → `falar("Conecte um fone Bluetooth")` + abre configurações Bluetooth.
7. `AudioDeviceCallback` permanece ativo monitorando conexões e desconexões.

---

## 6. Permissões e Configurações do Manifesto

- `android.hardware.type.watch` — declara o app como destinado a Wear OS
- `android.permission.BODY_SENSORS` — permite acesso a sensores corporais do relógio
- `android.permission.WAKE_LOCK` — mantém o processador ativo durante operações de áudio
- `com.google.android.wearable.standalone = true` — define o app como standalone, sem necessidade de app pareado no celular
- `Theme.DeviceDefault.NoActionBar` — tema nativo do Wear OS, sem barra de ação

---

## 7. Limitações Conhecidas

- O emulador Wear OS não possui motor TTS instalado, exigindo o fallback para `MediaPlayer`. Em dispositivo físico com Google Play Services, o TTS funciona normalmente.
- O arquivo `res/raw/iniciado.mp3` é genérico. Em produção, seria ideal ter arquivos distintos para cada mensagem.
- Reconhecimento de voz (entrada) não foi implementado nesta versão, apenas saída de áudio.

---

## 8. Conclusão

O aplicativo cumpre todos os requisitos solicitados pelo professor, implementando detecção de saídas de áudio, callback dinâmico de dispositivos, redirecionamento para configurações Bluetooth e reprodução de áudio no Wear OS.

Além disso, foram necessárias adaptações técnicas extras para contornar limitações do ambiente de emulação e incompatibilidades de tema, tornando o app funcional e estável tanto no emulador quanto em dispositivos reais.
