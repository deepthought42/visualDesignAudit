# visualDesignAudit

# Deploying to GCP

gcloud auth print-access-token | sudo docker login   -u oauth2accesstoken   --password-stdin https://us-central1-docker.pkg.dev

sudo docker build --no-cache -t us-central1-docker.pkg.dev/cosmic-envoy-280619/visual-design-audit/0.0.2 .

sudo docker push us-central1-docker.pkg.dev/cosmic-envoy-280619/visual-design-audit/0.0.2