# NextRace

PWA independente para organizar corrida + musculação em ciclos contínuos orientados à próxima prova.

## O que já funciona
- plano inicial da meia de 15/11/2026;
- corrida + força no mesmo calendário;
- registro de distância, pace, RPE, dor e observações;
- painel de evolução e volume;
- cadastro de futuras provas;
- motor local para gerar novos ciclos (5 km, 10 km, 21,1 km e 42,2 km);
- comparação opcional com prova recente (equivalência de Riegel);
- progressão de volume com semanas de redução e taper;
- PWA offline e instalável;
- backup/importação JSON;
- dados guardados no dispositivo.

## Publicação simples no GitHub Pages
1. Envie todos os arquivos do app para a raiz.
2. No GitHub, abra **Settings → Pages**.
3. Em **Build and deployment**, selecione **Deploy from a branch**.
4. Selecione `main` e `/ (root)` e salve.
5. Abra o endereço publicado no Android/Chrome e use **Instalar app** / **Adicionar à tela inicial**.

## Observação sobre notificações
Uma PWA puramente local consegue mostrar notificação quando o app/navegador está ativo e autorizado. Agendamento confiável com o app totalmente fechado exige push/servidor ou empacotamento nativo, e não está incluído nesta versão.
