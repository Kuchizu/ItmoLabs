/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * Copyright (c) 2022 STMicroelectronics.
  * All rights reserved.
  *
  * This software is licensed under terms that can be found in the LICENSE file
  * in the root directory of this software component.
  * If no LICENSE file comes with this software, it is provided AS-IS.
  *
  ******************************************************************************
  */
/* USER CODE END Header */
/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include "usart.h"
#include "gpio.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <ctype.h>
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */
/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */
/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/

/* USER CODE BEGIN PV */
uint16_t GREEN = GPIO_PIN_13;
uint16_t YELLOW = GPIO_PIN_14;
uint16_t RED = GPIO_PIN_15;
uint16_t BLINKING_GREEN = 0;
uint8_t INT_ON = 1;
uint8_t INT_OFF = 0;

uint16_t current_light = 1;
uint32_t start_time;
uint32_t duration = 5000;
uint32_t duration_for_red;
uint32_t duration_for_yellow = 3000;
uint32_t blink_duration = 500;
uint32_t blink_count = 0;
uint8_t button_flag = 0;

uint8_t interrupts_mode = 0;
uint8_t is_writing_now = 0;
char read_buffer[100];
char write_buffer[100];
HAL_StatusTypeDef status;
char* cur_process_char = read_buffer;
char* cur_read_char = read_buffer;
char* transmit_from_pointer = write_buffer;
char* write_to_pointer = write_buffer;
/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
/* USER CODE BEGIN PFP */
static void wait(uint32_t ms);
static void turnAllOff(void);
static void turnSpecificLightOn(uint16_t light);
static void blinkSpecificLight(uint32_t count, uint16_t light, uint32_t blinkDuration);
static void blink(uint16_t light);

static int  check_starts_with(char* a, char* b);
static char* concat(char *s1, char *s2);
static void next_ptr(char **pointer, char *buffer, size_t bufsize);
static void write_char_to_buff(char c);
static void write_line(const char* str);
static void write_command_not_found(void);
static int  is_number(char* str);
static void process_symbol(void);
/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */
void wait(uint32_t ms)
{
  uint32_t t0 = HAL_GetTick();
  while ((HAL_GetTick() - t0) < ms) {}
}

void turnAllOff(void)
{
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, GPIO_PIN_RESET);
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, GPIO_PIN_RESET);
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_15, GPIO_PIN_RESET);
}

void turnSpecificLightOn(uint16_t light)
{
  turnAllOff();
  HAL_GPIO_WritePin(GPIOD, light, GPIO_PIN_SET);
}

void blinkSpecificLight(uint32_t count, uint16_t light, uint32_t blinkDuration)
{
  for(uint32_t i = 0; i < count; i++){
    turnAllOff();
    wait(blinkDuration);
    turnSpecificLightOn(light);
    wait(blinkDuration);
  }
}

static void blink(uint16_t light)
{
  HAL_GPIO_TogglePin(GPIOD, light);
}

int check_starts_with(char* a, char* b) {
  size_t i = 0;
  while (a[i] != '\0') {
    if (a[i] != b[i]) return 0;
    i++;
  }
  return 1;
}

char* concat(char *s1, char *s2) {
  char *result = malloc(strlen(s1) + strlen(s2) + 1);
  if (!result) return NULL;
  strcpy(result, s1);
  strcat(result, s2);
  return result;
}

void next_ptr(char **pointer, char *buffer, size_t bufsize) {
  if (*pointer >= buffer + bufsize - 1){
    *pointer = buffer;
  } else {
    (*pointer)++;
  }
}

void write_char_to_buff(char c) {
  *write_to_pointer = c;
  next_ptr(&write_to_pointer, write_buffer, sizeof(write_buffer));
}

void write_line(const char* str) {
  char tmp[128];
  snprintf(tmp, sizeof(tmp), "\r\n%.120s\r\n", str);
  if (interrupts_mode == 0) {
    HAL_UART_Transmit(&huart6, (uint8_t *)tmp, (uint16_t)strlen(tmp), 10);
  } else {
    for (size_t i = 0; tmp[i] != '\0'; i++) {
      write_char_to_buff(tmp[i]);
    }
  }
}

void write_command_not_found() {
  write_line("Command not found. Available: '?', set mode 1/2, set timeout X, set interrupts on/off");
}

int is_number(char* str) {
  if (!str || *str == '\0') return 0;
  for (size_t i = 0; str[i] != '\0'; i++) {
    if (!isdigit((unsigned char)str[i])) return 0;
  }
  return 1;
}

void process_symbol() {
  if (*cur_process_char == '\r') {
    *cur_process_char = '\0';
    char* command = strtok(read_buffer, " ");
    if (command == NULL) {
      cur_process_char = read_buffer;
      return;
    }
    if (strcmp(command, "?") == 0) {
      char answer[100];
      const char* light = "unknown";
      switch (current_light) {
        case GPIO_PIN_15: light = "red"; break;
        case GPIO_PIN_14: light = "yellow"; break;
        case GPIO_PIN_13: light = "green"; break;
        case 0:           light = "blinking green"; break;
      }
      uint8_t mode = (button_flag == 0 && current_light == RED) ? 1 : 2;
      char interrupts = (interrupts_mode == 1) ? 'I' : 'P';
      uint32_t timeout_s = (4 * duration) / 1000;
      snprintf(answer, sizeof(answer), "Light: %s, Mode: %u, Timeout: %lu, Interrupts: %c",
               light, mode, (unsigned long)timeout_s, interrupts);
      write_line(answer);
    } else if (strcmp(command, "set") == 0) {
      char* first_arg = strtok(NULL, " ");
      if (first_arg == NULL) {
        write_command_not_found();
      } else if (strcmp(first_arg, "mode") == 0) {
        char* mode = strtok(NULL, " ");
        if      (mode && strcmp(mode, "1") == 0) { button_flag = 0; duration_for_red = duration * 4; write_line("Entered mode 1"); }
        else if (mode && strcmp(mode, "2") == 0) { button_flag = 1; duration_for_red = duration;     write_line("Entered mode 2"); }
        else { write_command_not_found(); }
      } else if (strcmp(first_arg, "timeout") == 0) {
        char* timeout = strtok(NULL, " ");
        if (is_number(timeout)) {
          int new_dur_ms_total = atoi(timeout) * 1000;
          if (duration_for_red == duration)      duration_for_red = new_dur_ms_total / 4;
          else                                    duration_for_red = new_dur_ms_total;
          duration = new_dur_ms_total / 4;
          char msg[64];
          snprintf(msg, sizeof(msg), "New duration is %s", timeout);
          write_line(msg);
        } else {
          write_command_not_found();
        }
      } else if (strcmp(first_arg, "interrupts") == 0) {
        char* interrupts = strtok(NULL, " ");
        if (interrupts && strcmp(interrupts, "on") == 0) {
          interrupts_mode = 1;
          transmit_from_pointer = write_to_pointer;
          cur_read_char = read_buffer;
          write_line("Interrupt mode on");
          HAL_UART_Receive_IT(&huart6, (uint8_t *)cur_read_char, sizeof(char));
        } else if (interrupts && strcmp(interrupts, "off") == 0) {
          interrupts_mode = 0;
          HAL_UART_Abort_IT(&huart6);
          write_line("Interrupt mode off");
        } else {
          write_command_not_found();
        }
      } else {
        write_command_not_found();
      }
    } else {
      write_command_not_found();
    }
    cur_process_char = read_buffer;
  } else {
    next_ptr(&cur_process_char, read_buffer, sizeof(read_buffer));
  }
}
/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{
  /* USER CODE BEGIN 1 */
  /* USER CODE END 1 */

  /* MCU Configuration--------------------------------------------------------*/
  HAL_Init();

  /* USER CODE BEGIN Init */
  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */
  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_USART6_UART_Init();

  /* USER CODE BEGIN 2 */
  HAL_UART_DeInit(&huart6);
  huart6.Init.BaudRate = 57600;
  HAL_UART_Init(&huart6);

  duration_for_red = 4 * duration;
  current_light = RED;
  turnSpecificLightOn(RED);
  start_time = HAL_GetTick();

  if (interrupts_mode == 1) {
    HAL_UART_Receive_IT(&huart6, (uint8_t *)cur_read_char, sizeof(char));
  }
  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
    switch (interrupts_mode) {
      case 1:
        if (cur_process_char != cur_read_char) {
          process_symbol();
        }
        if (is_writing_now == 0) {
          if (transmit_from_pointer != write_to_pointer) {
            is_writing_now = 1;
            HAL_UART_Transmit_IT(&huart6, (uint8_t *)transmit_from_pointer, sizeof(char));
          }
        }
        break;
      case 0:
        status = HAL_UART_Receive(&huart6, (uint8_t *)cur_process_char, sizeof(char), 100);
        if (status == HAL_OK) {
          HAL_UART_Transmit(&huart6, (uint8_t *)cur_process_char, sizeof(char), 10);
          process_symbol();
        }
        break;
    }

    switch(current_light) {
      case GPIO_PIN_15:
        if (HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_15) == GPIO_PIN_RESET && button_flag == 0) {
          duration_for_red = duration;
          button_flag = 1;
        }
        if ((HAL_GetTick() - start_time) >= duration_for_red) {
          current_light = GREEN;
          duration_for_red = 4 * duration;
          button_flag = 0;
          turnSpecificLightOn(GREEN);
          start_time = HAL_GetTick();
        }
        break;
      case GPIO_PIN_14:
        if ((HAL_GetTick() - start_time) >= duration_for_yellow) {
          current_light = RED;
          turnSpecificLightOn(RED);
          start_time = HAL_GetTick();
        }
        break;
      case GPIO_PIN_13:
        if ((HAL_GetTick() - start_time) >= duration) {
          current_light = BLINKING_GREEN;
          turnAllOff();
          start_time = HAL_GetTick();
        }
        break;
      case 0:
        if ((HAL_GetTick() - start_time) >= blink_duration) {
          blink_count++;
          if (blink_count < 6) {
            blink(GREEN);
          } else {
            blink_count = 0;
            current_light = YELLOW;
            turnSpecificLightOn(YELLOW);
          }
          start_time = HAL_GetTick();
        }
        break;
      default:
        current_light = RED;
        turnSpecificLightOn(RED);
        start_time = HAL_GetTick();
        break;
    }
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE3);
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_NONE;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
}

/* USER CODE BEGIN 4 */
void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart) {
  if (huart == &huart6) {
    if (*cur_read_char == '\r'){
      cur_read_char = read_buffer;
    } else {
      write_char_to_buff(*cur_read_char);
      next_ptr(&cur_read_char, read_buffer, sizeof(read_buffer));
    }
    if (interrupts_mode == 1)
      HAL_UART_Receive_IT(&huart6, (uint8_t *) cur_read_char, sizeof(char));
  }
}

void HAL_UART_TxCpltCallback(UART_HandleTypeDef *huart) {
  if (huart == &huart6) {
    is_writing_now = 0;
    next_ptr(&transmit_from_pointer, write_buffer, sizeof(write_buffer));
  }
}
/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  __disable_irq();
  while (1) {}
}

#ifdef  USE_FULL_ASSERT
void assert_failed(uint8_t *file, uint32_t line)
{
  (void)file; (void)line;
}
#endif /* USE_FULL_ASSERT */
