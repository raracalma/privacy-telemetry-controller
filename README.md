# Privacy & Telemetry Controller (Android)

Aplicativo Android em Kotlin + Jetpack Compose para apoiar o endurecimento de privacidade no aparelho, com uma base de integração com Shizuku e compilação de APK via GitHub Actions.

## Estado atual

**Protótipo inicial de interface (v0.1.0).** Esta versão ainda não bloqueia tráfego, não modifica AppOps e não desativa pacotes. A tela informa o estado básico do Shizuku e documenta as próximas etapas.

## Objetivos

- Revisar controles de privacidade e diagnósticos do Android/Samsung.
- Integrar operações privilegiadas via Shizuku quando suportadas e autorizadas.
- Implementar regras de bloqueio de domínios usando um mecanismo de VPN local, com transparência sobre o que foi realmente bloqueado.
- Exigir confirmação antes de alterações de sistema e manter registro local das ações.

## Requisitos

- Android Studio recente.
- JDK 17.
- Gradle 8.7.
- Android SDK Platform 35.
- Galaxy S22 ou outro dispositivo Android compatível para testes.
- Shizuku instalado e iniciado por depuração sem fio/ADB, quando necessário.

## Compilar localmente

Abra a pasta no Android Studio e aguarde a sincronização do Gradle. Ou, com Android SDK configurado:

```bash
gradle assembleDebug
```

APK de depuração:
`app/build/outputs/apk/debug/app-debug.apk`

## Compilação automática

O workflow `.github/workflows/build-apk.yml` compila em pushes para `main`, pull requests e execução manual. O APK aparece na aba **Actions > workflow run > Artifacts**. Ao enviar uma tag `v*`, o workflow também anexa o APK a uma GitHub Release.

Exemplo:

```bash
git tag v0.1.0
git push origin v0.1.0
```

## Segurança e limitações

- Shizuku não é root. Só pode executar operações permitidas ao processo autorizado pelo serviço.
- Alterar AppOps ou desativar pacotes pode quebrar notificações, login, atualizações ou funções essenciais. Implementar apenas operações verificadas, reversíveis e confirmadas pelo usuário.
- Uma VPN local pode filtrar conexões de rede, mas normalmente compete com outros aplicativos VPN.
- DNS filtrante não consegue bloquear toda telemetria, especialmente quando apps usam IPs diretos, domínios compartilhados ou técnicas alternativas.
- O Android limita quais identificadores e propriedades de hardware apps comuns podem acessar. O app não promete ocultar informações que o sistema não permite mascarar.
- Não inclui analytics, publicidade, conta ou servidor próprio.

## Próximas etapas

1. Confirmar compilação no GitHub Actions.
2. Adicionar listagem de pacotes instalados e busca.
3. Implementar leitura segura de AppOps disponíveis, com explicação de cada operação.
4. Implementar alterações individuais com confirmação e opção de reversão.
5. Projetar o filtro de tráfego como VPN local, com lista de bloqueio atualizável e logs locais.
6. Adicionar testes e documentação por versão do Android/One UI.
