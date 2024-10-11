# visualDesignAudit

# Deploying to GCP

Before deploying, make sure you have the necessary credentials:

## Retrieving GCP Project Credentials JSON

1. Go to the Google Cloud Console (https://console.cloud.google.com/).
2. Select your project.
3. Navigate to "IAM & Admin" > "Service Accounts".
4. Click on "Create Service Account" or select an existing one.
5. If creating a new account, give it a name and grant it the necessary permissions.
6. On the service account details page, go to the "Keys" tab.
7. Click "Add Key" > "Create new key".
8. Choose JSON as the key type and click "Create".
9. The JSON file will be downloaded to your computer. Keep it secure.

## Retrieving Gmail Credentials JSON

1. Go to the Google Cloud Console (https://console.cloud.google.com/).
2. Select your project.
3. Navigate to "APIs & Services" > "Credentials".
4. Click "Create Credentials" > "OAuth client ID".
5. Select "Desktop app" as the application type.
6. Click "Create" and download the JSON file.
7. Rename the file to `credentials.json` and place it in your project directory.

Once you have these credentials, you can proceed with the deployment:

gcloud auth print-access-token | sudo docker login -u oauth2accesstoken --password-stdin https://us-central1-docker.pkg.dev

sudo docker build --no-cache -t us-central1-docker.pkg.dev/cosmic-envoy-280619/visual-design-audit/0.0.2 .

sudo docker push us-central1-docker.pkg.dev/cosmic-envoy-280619/visual-design-audit/0.0.2

## Adding Secrets to GCP and Setting Required Values

Before deploying, you need to add certain secrets to Google Cloud Secret Manager and set the required values based on your application.properties and auth0.properties files.

### Adding Secrets to GCP Secret Manager

1. Go to the Google Cloud Console (https://console.cloud.google.com/).
2. Select your project.
3. Navigate to "Security" > "Secret Manager".
4. Click "Create Secret" for each of the following secrets:

   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `AUTH0_CLIENT_ID`
   - `AUTH0_CLIENT_SECRET`
   - `AUTH0_DOMAIN`
   - `GCP_PROJECT_ID`
   - `GCP_BUCKET_NAME`

5. For each secret, provide a name and the corresponding value from your application.properties or auth0.properties file.

### Required Values

Set the following values in your secrets:

1. From application.properties:
   - `SPRING_DATASOURCE_URL`: Your database URL (e.g., jdbc:postgresql://[HOST]:[PORT]/[DATABASE])
   - `SPRING_DATASOURCE_USERNAME`: Your database username
   - `SPRING_DATASOURCE_PASSWORD`: Your database password
   - `GCP_PROJECT_ID`: Your Google Cloud Project ID
   - `GCP_BUCKET_NAME`: The name of your Google Cloud Storage bucket

2. From auth0.properties:
   - `AUTH0_CLIENT_ID`: Your Auth0 client ID
   - `AUTH0_CLIENT_SECRET`: Your Auth0 client secret
   - `AUTH0_DOMAIN`: Your Auth0 domain (e.g., your-domain.auth0.com)

### Accessing Secrets in Your Application

To access these secrets in your application, you'll need to update your configuration to use the Secret Manager. Here's an example of how to do this:

