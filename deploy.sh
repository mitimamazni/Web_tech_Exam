#!/bin/bash

# Update system
sudo apt-get update
sudo apt-get upgrade -y

# Install Java 17
sudo apt-get install -y openjdk-17-jdk

# Install MySQL
sudo apt-get install -y mysql-server-8.0

# Install Nginx
sudo apt-get install -y nginx

# Install certbot for SSL
sudo apt-get install -y certbot python3-certbot-nginx

# Create application directory
sudo mkdir -p /opt/ecommerce
sudo mkdir -p /var/www/ecommerce/uploads
sudo chown -R $USER:$USER /opt/ecommerce
sudo chown -R $USER:$USER /var/www/ecommerce

# Create service user
sudo useradd -r -s /bin/false ecommerce

# Copy application files
cp target/*.jar /opt/ecommerce/app.jar
sudo chown ecommerce:ecommerce /opt/ecommerce/app.jar

# Create systemd service
cat << EOF | sudo tee /etc/systemd/system/ecommerce.service
[Unit]
Description=E-commerce Application
After=network.target mysql.service

[Service]
Type=simple
User=ecommerce
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -jar /opt/ecommerce/app.jar
Environment="DB_URL=jdbc:mysql://localhost:3306/ecommercedb"
Environment="DB_USERNAME=ecommerce_user"
Environment="DB_PASSWORD=your_password"
Environment="JWT_SECRET=your_secret_key"
Environment="EMAIL_FROM=noreply@yourdomain.com"
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# Generate self-signed SSL certificate
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/nginx-selfsigned.key \
    -out /etc/ssl/certs/nginx-selfsigned.crt \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"

# Generate strong DH group
sudo openssl dhparam -out /etc/nginx/dhparam.pem 2048

# Secure SSL private key
sudo chmod 600 /etc/ssl/private/nginx-selfsigned.key

# Configure Nginx
cat << EOF | sudo tee /etc/nginx/sites-available/ecommerce
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;

    # Redirect HTTP to HTTPS
    return 301 https://\$host\$request_uri;
}

server {
    listen 443 ssl default_server;
    listen [::]:443 ssl default_server;
    server_name _;

    # Self-signed certificate paths
    ssl_certificate /etc/ssl/certs/nginx-selfsigned.crt;
    ssl_certificate_key /etc/ssl/private/nginx-selfsigned.key;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /static/ {
        alias /opt/ecommerce/static/;
        expires 7d;
        add_header Cache-Control "public, no-transform";
    }

    location /uploads/ {
        alias /var/www/ecommerce/uploads/;
        expires 7d;
        add_header Cache-Control "public, no-transform";
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/ecommerce /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test and restart Nginx
sudo nginx -t
sudo systemctl restart nginx

# Start application service
sudo systemctl enable ecommerce
sudo systemctl start ecommerce
