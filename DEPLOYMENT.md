# Déploiement de l'API Songoy Collection

Le workflow [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml) se déclenche
sur un push vers la branche `deploy` ou manuellement avec **Run workflow**. Il construit
l'image dans GitHub Actions, l'envoie au VPS puis recrée uniquement le conteneur de l'API.
La base MySQL n'est volontairement pas créée par ce déploiement : l'application est déjà
configurée pour se connecter à une base existante via `DB_HOST`.

La branche `deploy` n'existe pas encore dans ce dépôt (seule `main` existe) : créez-la et
publiez-la avant d'attendre un déploiement automatique.

## Prérequis vérifiés

- Le projet demande Java 22 (`pom.xml`). L'image de build utilise donc Maven/Temurin 22
  et le runtime utilise Temurin 22.
- Le code applicatif est dans `songoycollection/`, tandis que le dépôt Git est ce dossier
  parent. Le `Dockerfile` est placé à la racine du dépôt pour que GitHub Actions le voie.
- Le VPS doit disposer de Docker Engine et du plugin `docker compose`, et l'utilisateur
  SSH doit pouvoir les exécuter.
- Une base MySQL accessible depuis le conteneur et le répertoire persistant des fichiers
  envoyés doivent exister avant le premier déploiement.

## Configuration GitHub

Ajoutez ces **secrets** au dépôt :

- `SSH_PRIVATE_KEY` : clé privée du compte de déploiement du VPS ;
- `VPS_HOST` et `VPS_USER` ;
- `VPS_KNOWN_HOSTS` : ligne(s) `known_hosts` du VPS, obtenue(s) et vérifiée(s) hors bande.

## Préparation unique du VPS

Sur le VPS, créez `~/app/songoycollection` pour l'utilisateur de déploiement et copiez
`.env.example` vers `~/app/songoycollection/.env`. Remplacez toutes les valeurs d'exemple, en particulier
`DB_*`, `CORS_ALLOWED_ORIGINS`, `APP_HOST_PORT` et `APP_UPLOADS_DIR`. Créez aussi le
répertoire défini par `APP_UPLOADS_DIR` et donnez-lui les permissions nécessaires à
Docker. Les dix variables `STOCK_SEUIL_*` sont également obligatoires dans le code
actuel : renseignez les seuils métier validés, sans quoi l'application ne démarrera pas.

### Base MySQL installée sur le VPS

Le conteneur API utilise le réseau Docker dédié `songoycollection-network`
(`172.26.0.0/24`). Si MySQL est installé directement sur le VPS, laissez
`DB_HOST=host.docker.internal` et accordez les droits à l'utilisateur applicatif
depuis ce sous-réseau uniquement :

```sql
RENAME USER 'boutique_app'@'172.17.0.1' TO 'boutique_app'@'172.26.0.%';
SHOW GRANTS FOR 'boutique_app'@'172.26.0.%';
```

Le premier déploiement crée le réseau Docker. L'adresse source de l'API sera alors
dans `172.26.0.0/24`, sans rendre l'utilisateur MySQL accessible depuis Internet.

Le workflow vérifie l'existence de ce `.env` et ne le transfère jamais. Ainsi, les secrets
de production et les fichiers envoyés restent sur le VPS à chaque déploiement.

## Point de sécurité

Un fichier `songoycollection/.env` est actuellement suivi par Git malgré la règle
`.gitignore`. Il n'est pas inclus dans l'image grâce au `.dockerignore`, mais toute valeur
sensible déjà committée doit être considérée exposée : faites une rotation des secrets,
puis retirez ce fichier de l'index Git (`git rm --cached songoycollection/.env`) dans un
commit séparé tout en conservant le fichier local.
