variable "aws_region" {
  description = "Region AWS para desplegar los recursos."
  type        = string
  default     = "us-east-1"
}

variable "s3_bucket_name" {
  description = "Nombre unico del bucket S3 para almacenar inventarios."
  type        = string
}

variable "ses_sender_email" {
  description = "Correo verificado en SES que se usara como remitente."
  type        = string
  default     = ""
}
