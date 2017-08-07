/*
* This is a simple socket server demo which
* used for communication with remote client.
* 2017/07/07 Qing
*/
#include <stdio.h>  
#include <stdlib.h>  
#include <strings.h>  
#include <sys/types.h>  
#include <sys/socket.h> 
#include <sys/types.h>          /* See NOTES */
#include <sys/socket.h> 
#include <memory.h>  
#include <unistd.h>  
//#include <linux/in.h>  
#include <netinet/in.h>  
//#include <linux/inet_diag.h>  
#include <arpa/inet.h>  
#include <signal.h>  
      
    
#define PORT    8989   //define the socket port  
#define BACKLOG 5       //the length of listen frame   
#define buflen  1024  
      
void process_conn_server(int s);  
void sig_pipe(int signo);  
      
int ss,sc;  //ss is server socket desscripter，sc is a client's socket descriptor  
      
int main(int argc,char *argv[])  
{  
      
	struct sockaddr_in server_addr; //server socket  
	struct sockaddr_in client_addr; 
      
	int err;     
	pid_t pid;   
      
	/*****************socket()***************/  
	ss = socket(AF_INET,SOCK_STREAM,0); //create a stream socket.  
	if(ss<0)  
	{  
    	printf("server : server socket create error\n");  
        return -1;  
    }  
	//register the signal  
	signal(SIGTSTP,sig_pipe);  
       
	/******************bind()****************/  
	//initial the socket address structure. 
	memset(&server_addr,0,sizeof(server_addr));  
	server_addr.sin_family = AF_INET;           //protocol family  
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);   //local Address.  
	server_addr.sin_port = htons(PORT);  
      
	err = bind(ss,(struct sockaddr *)&server_addr,sizeof(struct sockaddr));  
	if(err<0) {  
    	printf("server : bind error\n");  
    	return -1;  
    }  
      
    /*****************listen()***************/  
	err = listen(ss,BACKLOG);   //set the listen queue.  
	if(err < 0){  
    	printf("server : listen error\n");  
    	return -1;  
    }  
      
  
      
	for(;;){  
    	socklen_t addrlen = sizeof(client_addr);  
    	//accept the client socket 
    	sc = accept(ss,(struct sockaddr *)&client_addr,&addrlen);  
    	if(sc < 0) {  
         	continue;     
        }else{  
        	printf("server : connected\n");  
        }  
      
            //Create the child process，which  is used for communication with client  
		pid = fork();  
        if(pid == 0){  
        	close(ss);  
        	process_conn_server(sc);  
        }else {  
        	close(sc);  
        }  
    }  
}  
      
      
//communication with client. 
void process_conn_server(int s)  
{  
	ssize_t size = 0;  
	char buffer[buflen];    
	for(;;){  
		//Waiting  
		memset(buffer, 0 , buflen);
    	// for(size = 0;size == 0 ;);  
		size = read(s,buffer,50);
    	//print the message from client. 
		printf("Size :%d\r\n",(int)size); 
     	printf("Data:%s\r\n",buffer);  
      
		//exit this process.  
		if(strcmp(buffer,"quit") == 0){  
    		close(s);   
    		return ;  
    	}  
			
		sprintf(buffer,"%d bytes Recieved\n",(int)size);  
		write(s,buffer,strlen(buffer)+1);  
	}  
}  
    
void sig_pipe(int signo)  
{  
	printf("catch a signal\n");  
	if(signo == SIGTSTP){  
		printf("recieved SIGTSTP Signal\n");  
		int ret1 = close(ss);  
		int ret2 = close(sc);  
		int ret = ret1>ret2?ret1:ret2;  
		if(ret == 0)  
        	printf("Success : Close the socket\n");  
        else if(ret ==-1 )  
        	printf("Failed : Close socket failed\n");  
      
        exit(1);  
    }  
}  
