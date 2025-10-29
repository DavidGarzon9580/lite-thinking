# Terraform Infra

Plan base para crear los recursos minimos requeridos por la plataforma:

- Bucket S3 con versionamiento y cifrado para almacenar los PDF de inventario.
- Identidad de correo en AWS SES opcional (requiere verificacion manual posterior).

## Uso rapido

```bash
cd infra/terraform
terraform init
terraform apply -var "s3_bucket_name=litethinking-inventory-prod" -var "ses_sender_email=no-reply@tu-dominio.com"
```

> Nota: si aun estas en modo sandbox de SES, asegúrate de verificar la identidad y destinatarios manualmente desde la consola de AWS.

Los recursos creados pueden referenciarse en el backend mediante las variables de entorno `AWS_S3_BUCKET`, `AWS_REGION` y `AWS_SES_SENDER`.
