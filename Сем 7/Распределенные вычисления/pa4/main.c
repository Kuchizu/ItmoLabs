#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <string.h>
#include <unistd.h>

#include "common.h"
#include "ipc.h"
#include "pa2345.h"
#include "message_handler.h"
#include "pipe_manager.h"

extern int started_len;
extern int done_len;
extern char free_payload[MAX_PAYLOAD_LEN];
extern timestamp_t lamport_time;
extern int8_t num_running_processes;

FILE *eventlog;
int mutexl = 0;
char utexl[6] = "mutexl";
int im = 0;
int8_t num_processes = 0;

void child(int8_t num_processes, local_id id)
{
	num_running_processes = num_processes;
	lamport_time = id;
	process_send_multicast(id, STARTED);
	timestamp_t time = get_lamport_time();
	printf(log_started_fmt,
	       time,
	       id,
	       getpid(),
	       getppid(),
	       0);
	fprintf(eventlog,
	        log_started_fmt,
	        time,
	        id,
	        getpid(),
	        getppid(),
	        0);
	process_recieve_all(num_processes, id, STARTED);
	time = get_lamport_time();
	printf(log_received_all_started_fmt, time, id);
	fprintf(eventlog, log_received_all_started_fmt, time, id);
	process_load(id);
	time = get_lamport_time() + 1;
	printf(log_done_fmt, time, id, 0);
	fprintf(eventlog, log_done_fmt, time, id, 0);
	process_send_multicast(id, DONE);
	process_recieve_all(num_processes, id, DONE);
	time = get_lamport_time();
	printf(log_received_all_done_fmt, time, id);
	fprintf(eventlog, log_received_all_done_fmt, time, id);
}

// Создание дочерних процессов
void create_childs(int8_t num_processes)
{
	local_id i;
	for (i=0; i<num_processes; i++)
	{
		int16_t child_pid;
		child_pid = fork();
		if (child_pid == -1)
		{
			fclose(eventlog);
			perror("fork");
			exit(EXIT_FAILURE);
		}
		if (child_pid == 0)
		{
		    close_unused_pipes(num_processes, i+1);
		    child(num_processes, i+1);
			close_used_pipes(num_processes, i+1);
			fclose(eventlog);
			exit(EXIT_SUCCESS);
		}
	}
}

void wait_for_childs(int8_t num_processes)
{
	while (num_processes > 0)
	{
		int16_t w;
		w = waitpid(-1, NULL, WNOHANG);
		if (w > 0)
		    num_processes--;
		if (w == -1)
		{
			fclose(eventlog);
			perror("wait");
			exit(EXIT_FAILURE);
		}
        process_recieve_any(PARENT_ID);
	}
}

void parent(int8_t num_processes, FILE *pipelog)
{
	create_pipe_topology(num_processes, pipelog);
	create_childs(num_processes);
	close_unused_pipes(num_processes, PARENT_ID);
    wait_for_childs(num_processes);
	close_used_pipes(num_processes, PARENT_ID);
}

void set_opts(int argc, char *argv[], char opt, int8_t *num_processes)
{
    switch (opt)
	{
		case 'p':
		    if ((*num_processes = atoi(argv[optind])) < 0)
		    {
		        fprintf(stderr, "Usage: %s [-p numofprocesses][-mutexl]\n", argv[0]);
                exit(EXIT_FAILURE);
			}
			break;
		case 'm':
		case 'u':
		case 't':
		case 'e':
		case 'x':
		case 'l':
			if (im < 6 && utexl[im] == opt)
		    {
				if (utexl[im] == 'l')
				{
			        mutexl++;
				}
				im++;
			}
			else
		    {
		        fprintf(stderr, "Usage: %s [-p numofprocesses][-mutexl]\n", argv[0]);
                exit(EXIT_FAILURE);
		    }
			break;
	}
}

int main(int argc, char *argv[])
{
	const char *opts = "p-mutexl";
	char opt;
	FILE *pipelog;
	if(argc != 3 && argc != 4) {
        fprintf(stderr, "Usage: %s [-p numofprocesses] [-mutexl]\n", argv[0]);
        exit(EXIT_FAILURE);
    }
	while ((opt = getopt(argc,argv,opts)) != -1)
	{
		set_opts(argc, argv, opt, &num_processes);
	}
	if (argc == 4 && im < 6)
	{
        fprintf(stderr, "Usage: %s [-p numofprocesses] [-mutexl]\n", argv[0]);
        exit(EXIT_FAILURE);
	}
	if (!(pipelog = fopen(pipes_log, "w")) || !(eventlog = fopen(events_log, "a")))
	{
		perror("fopen");
		exit(EXIT_FAILURE);
	}
	sprintf(free_payload, log_started_fmt, 255, PARENT_ID, getpid(), getppid(), 0);
	started_len = strlen(free_payload);
	sprintf(free_payload, log_done_fmt, 255, PARENT_ID, 0);
	done_len = strlen(free_payload);
    parent(num_processes, pipelog);
	fclose(eventlog);
	return 0;
}

