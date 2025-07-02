pipeline {
    agent any
    
    environment {
        // Use container name instead of localhost since Jenkins runs in Docker
        SONAR_HOST_URL = 'http://casecashback-sonarqube:9000'
        SONAR_LOGIN = 'admin'
        SONAR_PASSWORD = 'admin'
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    // Clean workspace before checkout
                    deleteDir()
                    
                    // Alternative: Use git step with explicit URL and credentials
                    retry(3) {
                        timeout(time: 10, unit: 'MINUTES') {
                            // Option 1: Use checkout scm with better error handling
                            try {
                                checkout([
                                    $class: 'GitSCM',
                                    branches: scm.branches,
                                    doGenerateSubmoduleConfigurations: false,
                                    extensions: [
                                        [$class: 'CloneOption', 
                                         depth: 1, 
                                         noTags: false, 
                                         reference: '', 
                                         shallow: true,
                                         timeout: 20],
                                        [$class: 'CheckoutOption', timeout: 20],
                                        [$class: 'CleanBeforeCheckout'],
                                        [$class: 'CleanCheckout']
                                    ],
                                    submoduleCfg: [],
                                    userRemoteConfigs: scm.userRemoteConfigs
                                ])
                            } catch (Exception e) {
                                echo "Checkout failed: ${e.getMessage()}"
                                // Option 2: Fallback to git command
                                if (isUnix()) {
                                    sh 'git --version'
                                    sh 'git config --global http.postBuffer 1048576000'
                                    sh 'git config --global http.maxRequestBuffer 100M'
                                    sh 'git config --global core.preloadindex true'
                                } else {
                                    bat 'git --version'
                                    bat 'git config --global http.postBuffer 1048576000'
                                    bat 'git config --global http.maxRequestBuffer 100M'
                                    bat 'git config --global core.preloadindex true'
                                }
                                throw e
                            }
                        }
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean compile'
                    } else {
                        bat 'mvnw.cmd clean compile'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    script {
                        try {
                            publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                        } catch (Exception e) {
                            echo "Coverage publishing failed: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests'
                    } else {
                        bat 'mvnw.cmd package -DskipTests'
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        if (isUnix()) {
                            sh """
                                ./mvnw sonar:sonar \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_LOGIN} \
                                -Dsonar.password=${SONAR_PASSWORD} \
                                -Dsonar.projectKey=cashback-security-jwt \
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        } else {
                            bat """
                                mvnw.cmd sonar:sonar ^
                                -Dsonar.host.url=%SONAR_HOST_URL% ^
                                -Dsonar.login=%SONAR_LOGIN% ^
                                -Dsonar.password=%SONAR_PASSWORD% ^
                                -Dsonar.projectKey=cashback-security-jwt ^
                                -Dsonar.projectName="Cashback Security JWT"
                            """
                        }
                        echo "SonarQube analysis completed successfully!"
                        echo "View results at: ${SONAR_HOST_URL}/dashboard?id=cashback-security-jwt"
                    } catch (Exception e) {
                        echo "SonarQube analysis failed: ${e.getMessage()}"
                        echo "This might be due to SonarQube not being fully ready or network issues."
                        echo "Pipeline will continue as UNSTABLE..."
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '🎉 Pipeline completed successfully!'
            echo "SonarQube Dashboard: http://localhost:9000"
        }
        failure {
            echo '❌ Pipeline failed!'
        }
        unstable {
            echo '⚠️ Pipeline completed with warnings (e.g., SonarQube analysis issues)'
        }
    }
}