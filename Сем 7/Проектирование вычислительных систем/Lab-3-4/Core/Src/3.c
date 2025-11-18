/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2022 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include "ctype.h"
#include "stdbool.h"
#include "stdint.h"
#include "stdio.h"
#include "string.h"
#include "math.h"
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
#define BUF_SIZE 256

// Частоты нот первой октавы (пятой по порядку) в Гц
#define NOTE_C_BASE  261.63f
#define NOTE_D_BASE  293.67f
#define NOTE_E_BASE  329.63f
#define NOTE_F_BASE  349.23f
#define NOTE_G_BASE  392.00f
#define NOTE_A_BASE  440.00f
#define NOTE_B_BASE  493.88f

// Количество октав (0 - субконтроктава, 4 - первая октава, 8 - пятая октава)
#define MIN_OCTAVE 0
#define MAX_OCTAVE 8
#define DEFAULT_OCTAVE 4  // Первая октава (пятая по порядку)

// Длительность воспроизведения
#define MIN_DURATION 1    // 0.1 секунды (в единицах по 0.1с)
#define MAX_DURATION 50   // 5.0 секунд (в единицах по 0.1с)
#define DEFAULT_DURATION 10  // 1.0 секунда (в единицах по 0.1с)

// Частота таймера TIM1 (на шине APB2)
// SYSCLK = 180 МГц, APB2 = SYSCLK/2 = 90 МГц
#define TIM1_FREQ 90000000  // 90 МГц
/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
I2C_HandleTypeDef hi2c1;

TIM_HandleTypeDef htim1;
TIM_HandleTypeDef htim6;

UART_HandleTypeDef huart6;

/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_TIM1_Init(void);
static void MX_TIM6_Init(void);
static void MX_USART6_UART_Init(void);
static void MX_I2C1_Init(void);
/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

// ----- Глобальные переменные -----
static uint8_t current_octave = DEFAULT_OCTAVE;   // Текущая октава (4 = первая октава)
static uint8_t note_duration = DEFAULT_DURATION;  // Длительность ноты в единицах по 0.1с
static volatile bool is_playing = false;          // Флаг воспроизведения
static volatile uint32_t play_counter = 0;        // Счетчик для длительности в миллисекундах
static volatile uint32_t tim6_counter = 0;        // Счетчик миллисекунд для TIM6
static volatile bool play_all_notes_requested = false; // Флаг запроса воспроизведения всех нот
static char rx_buffer[1];                         // Буфер приема UART

// Базовые частоты нот первой октавы
static const float base_frequencies[7] = {
    NOTE_C_BASE,  // До  (1)
    NOTE_D_BASE,  // Ре  (2)
    NOTE_E_BASE,  // Ми  (3)
    NOTE_F_BASE,  // Фа  (4)
    NOTE_G_BASE,  // Соль(5)
    NOTE_A_BASE,  // Ля  (6)
    NOTE_B_BASE   // Си  (7)
};

// Названия нот
static const char* note_names[7] = {
    "До", "Ре", "Ми", "Фа", "Соль", "Ля", "Си"
};

// Названия октав
static const char* octave_names[9] = {
    "Субконтроктава",
    "Контроктава",
    "Большая октава",
    "Малая октава",
    "Первая октава",
    "Вторая октава",
    "Третья октава",
    "Четвертая октава",
    "Пятая октава"
};

// ----- Вспомогательные функции -----

// Функция для вычисления частоты ноты для заданной октавы
float get_note_frequency(uint8_t note_index, uint8_t octave) {
    // note_index: 0-6 (До-Си)
    // octave: 0-8 (субконтроктава - пятая октава)
    // Первая октава имеет индекс 4

    float base_freq = base_frequencies[note_index];
    int octave_shift = (int)octave - 4;  // Сдвиг относительно первой октавы

    // Частота изменяется в 2 раза при изменении октавы на 1
    float frequency = base_freq * powf(2.0f, (float)octave_shift);

    return frequency;
}

// Функция для отправки строки через UART
void uart_send_string(const char* str) {
    HAL_UART_Transmit(&huart6, (uint8_t*)str, strlen(str), 1000);
}

// Функция для воспроизведения ноты
void play_note(uint8_t note_index, uint8_t octave, uint8_t duration_units) {
    float frequency = get_note_frequency(note_index, octave);
    uint32_t freq_int = (uint32_t)frequency;

    // Настройка таймера для генерации ШИМ с заданной частотой
    // Используем ту же формулу, что и в оригинальном коде
    // ARR = TIM1_FREQ / (frequency * PSC) - 1
    htim1.Instance->ARR = TIM1_FREQ / (freq_int * htim1.Instance->PSC) - 1;
    htim1.Instance->CCR1 = htim1.Instance->ARR >> 1;  // 50% скважность

    // Установка длительности воспроизведения (duration_units в единицах по 0.1с = 100мс)
    play_counter = duration_units * 100;  // Переводим в миллисекунды
    is_playing = true;
}

// Функция для остановки воспроизведения
void stop_note(void) {
    htim1.Instance->CCR1 = 0;
    is_playing = false;
    play_counter = 0;
}

// Callback для UART приема
void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart) {
    if (huart->Instance == USART6) {
        char received_char = rx_buffer[0];

        // Эхо (кроме Enter)
        if (received_char != '\r' && received_char != '\n') {
            HAL_UART_Transmit(&huart6, (uint8_t*)rx_buffer, 1, 100);
        }

        // Обработка команды
        if (received_char >= '1' && received_char <= '7') {
            // Воспроизведение ноты
            handle_note_command(received_char);
        }
        else if (received_char == '+' || received_char == '-') {
            // Изменение октавы
            handle_octave_change(received_char);
        }
        else if (received_char == 'A' || received_char == 'a') {
            // Изменение длительности
            handle_duration_change(received_char);
        }
        else if (received_char == '\r' || received_char == '\n') {
            // Воспроизведение всех нот октавы
            play_all_notes_in_octave();
        }
        else {
            // Неизвестная команда
            handle_unknown_command(received_char);
        }

        // Перезапуск приема
        HAL_UART_Receive_IT(&huart6, (uint8_t*)rx_buffer, 1);
    }
}

// --------------------- TIMER ---------------------

// Callback таймера TIM6 (вызывается каждую 1 мс)
void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef * htim) {
    if (htim->Instance == TIM6) {
        tim6_counter++;

        if (is_playing && play_counter > 0) {
            play_counter--;
            if (play_counter == 0) {
                stop_note();
            }
        }
    }
}

// --------------------- Обработка команд ---------------------

// Функция обработки команды воспроизведения ноты
void handle_note_command(char note_char) {
    uint8_t note_index = note_char - '1';  // '1'-'7' -> 0-6

    if (note_index < 7) {
        char msg[128];
        sprintf(msg, "\r\nИграет нота: %s, октава: %s, длительность: %.1f с\r\n",
                note_names[note_index],
                octave_names[current_octave],
                (float)note_duration / 10.0f);
        uart_send_string(msg);

        play_note(note_index, current_octave, note_duration);
    }
}

// Функция обработки команды изменения октавы
void handle_octave_change(char direction) {
    if (direction == '+') {
        if (current_octave < MAX_OCTAVE) {
            current_octave++;
            char msg[128];
            sprintf(msg, "\r\nОктава: %s, длительность: %.1f с\r\n",
                    octave_names[current_octave],
                    (float)note_duration / 10.0f);
            uart_send_string(msg);
        }
    } else if (direction == '-') {
        if (current_octave > MIN_OCTAVE) {
            current_octave--;
            char msg[128];
            sprintf(msg, "\r\nОктава: %s, длительность: %.1f с\r\n",
                    octave_names[current_octave],
                    (float)note_duration / 10.0f);
            uart_send_string(msg);
        }
    }
}

// Функция обработки команды изменения длительности
void handle_duration_change(char command) {
    if (command == 'A') {
        if (note_duration < MAX_DURATION) {
            note_duration++;
            char msg[128];
            sprintf(msg, "\r\nОктава: %s, длительность: %.1f с\r\n",
                    octave_names[current_octave],
                    (float)note_duration / 10.0f);
            uart_send_string(msg);
        }
    } else if (command == 'a') {
        if (note_duration > MIN_DURATION) {
            note_duration--;
            char msg[128];
            sprintf(msg, "\r\nОктава: %s, длительность: %.1f с\r\n",
                    octave_names[current_octave],
                    (float)note_duration / 10.0f);
            uart_send_string(msg);
        }
    }
}

// Функция для воспроизведения всех нот октавы
void play_all_notes_in_octave(void) {
    // Устанавливаем флаг запроса воспроизведения всех нот
    // Само воспроизведение будет выполнено в главном цикле
    play_all_notes_requested = true;

    char msg[128];
    sprintf(msg, "\r\nВоспроизведение всех нот октавы: %s, длительность: %.1f с\r\n",
            octave_names[current_octave],
            (float)note_duration / 10.0f);
    uart_send_string(msg);
}

// Функция обработки неизвестного символа
void handle_unknown_command(char c) {
    char msg[64];
    sprintf(msg, "\r\nНеверный символ: код %d (0x%02X)\r\n", (int)c, (unsigned char)c);
    uart_send_string(msg);
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

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */

  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_TIM1_Init();
  MX_TIM6_Init();
  MX_USART6_UART_Init();
  MX_I2C1_Init();
  /* USER CODE BEGIN 2 */

  // Запуск ШИМ для звука
  HAL_TIM_PWM_Start(&htim1, TIM_CHANNEL_1);

  // Запуск таймера TIM6 с прерыванием (100 мс)
  HAL_TIM_Base_Start_IT(&htim6);

  // Запуск приема UART в режиме прерывания
  HAL_UART_Receive_IT(&huart6, (uint8_t*)rx_buffer, 1);

  // Приветственное сообщение
  uart_send_string("\r\n");
  uart_send_string("===============================================\r\n");
  uart_send_string("  Музыкальная клавиатура STM32\r\n");
  uart_send_string("===============================================\r\n");
  uart_send_string("\r\n");
  uart_send_string("Команды:\r\n");
  uart_send_string("  '1'-'7': Воспроизвести ноты (До-Си)\r\n");
  uart_send_string("  '+': Увеличить октаву\r\n");
  uart_send_string("  '-': Уменьшить октаву\r\n");
  uart_send_string("  'A': Увеличить длительность на 0.1с\r\n");
  uart_send_string("  'a': Уменьшить длительность на 0.1с\r\n");
  uart_send_string("  'Enter': Воспроизвести все ноты октавы\r\n");
  uart_send_string("\r\n");

  char msg[128];
  sprintf(msg, "Текущая октава: %s\r\n", octave_names[current_octave]);
  uart_send_string(msg);
  sprintf(msg, "Текущая длительность: %.1f с\r\n", (float)note_duration / 10.0f);
  uart_send_string(msg);
  uart_send_string("\r\n");

  /* USER CODE END 2 */

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */

  while (1)
  {
      // Обработка запроса воспроизведения всех нот октавы
      if (play_all_notes_requested && !is_playing) {
          play_all_notes_requested = false;

          // Счетчик текущей ноты (статическая переменная сохраняет состояние)
          static uint8_t current_note_index = 0;

          // Если это начало последовательности, инициализируем счетчик
          static bool sequence_started = false;
          if (!sequence_started) {
              current_note_index = 0;
              sequence_started = true;
          }

          // Воспроизводим текущую ноту
          if (current_note_index < 7) {
              char msg[128];
              sprintf(msg, "Нота: %s\r\n", note_names[current_note_index]);
              uart_send_string(msg);

              play_note(current_note_index, current_octave, note_duration);
              current_note_index++;

              // Если не все ноты сыграны, устанавливаем флаг снова
              if (current_note_index < 7) {
                  play_all_notes_requested = true;
              } else {
                  // Все ноты сыграны
                  uart_send_string("\r\nВоспроизведение завершено\r\n");
                  sequence_started = false;
                  current_note_index = 0;
              }
          }
      }
    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */

  }
  /* USER CODE END 3 */
}

void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};

  /** Configure the main internal regulator output voltage
  */
  __HAL_RCC_PWR_CLK_ENABLE();
  __HAL_PWR_VOLTAGESCALING_CONFIG(PWR_REGULATOR_VOLTAGE_SCALE1);
  /** Initializes the CPU, AHB and APB busses clocks
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSE;
  RCC_OscInitStruct.HSEState = RCC_HSE_ON;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_ON;
  RCC_OscInitStruct.PLL.PLLSource = RCC_PLLSOURCE_HSE;
  RCC_OscInitStruct.PLL.PLLM = 15;
  RCC_OscInitStruct.PLL.PLLN = 216;
  RCC_OscInitStruct.PLL.PLLP = RCC_PLLP_DIV2;
  RCC_OscInitStruct.PLL.PLLQ = 4;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Activate the Over-Drive mode
  */
  if (HAL_PWREx_EnableOverDrive() != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB busses clocks
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_PLLCLK;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV4;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV2;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_5) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief I2C1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_I2C1_Init(void)
{

  /* USER CODE BEGIN I2C1_Init 0 */

  /* USER CODE END I2C1_Init 0 */

  /* USER CODE BEGIN I2C1_Init 1 */

  /* USER CODE END I2C1_Init 1 */
  hi2c1.Instance = I2C1;
  hi2c1.Init.ClockSpeed = 400000;
  hi2c1.Init.DutyCycle = I2C_DUTYCYCLE_2;
  hi2c1.Init.OwnAddress1 = 0;
  hi2c1.Init.AddressingMode = I2C_ADDRESSINGMODE_7BIT;
  hi2c1.Init.DualAddressMode = I2C_DUALADDRESS_DISABLE;
  hi2c1.Init.OwnAddress2 = 0;
  hi2c1.Init.GeneralCallMode = I2C_GENERALCALL_DISABLE;
  hi2c1.Init.NoStretchMode = I2C_NOSTRETCH_DISABLE;
  if (HAL_I2C_Init(&hi2c1) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure Analogue filter
  */
  if (HAL_I2CEx_ConfigAnalogFilter(&hi2c1, I2C_ANALOGFILTER_ENABLE) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure Digital filter
  */
  if (HAL_I2CEx_ConfigDigitalFilter(&hi2c1, 0) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN I2C1_Init 2 */

  /* USER CODE END I2C1_Init 2 */

}

/**
  * @brief TIM1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM1_Init(void)
{

  /* USER CODE BEGIN TIM1_Init 0 */

  /* USER CODE END TIM1_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};
  TIM_OC_InitTypeDef sConfigOC = {0};
  TIM_BreakDeadTimeConfigTypeDef sBreakDeadTimeConfig = {0};

  /* USER CODE BEGIN TIM1_Init 1 */

  /* USER CODE END TIM1_Init 1 */
  htim1.Instance = TIM1;
  htim1.Init.Prescaler = 89;
  htim1.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim1.Init.Period = 999;
  htim1.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim1.Init.RepetitionCounter = 0;
  htim1.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim1, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  if (HAL_TIM_PWM_Init(&htim1) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim1, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sConfigOC.OCMode = TIM_OCMODE_PWM1;
  sConfigOC.Pulse = 500;
  sConfigOC.OCPolarity = TIM_OCPOLARITY_HIGH;
  sConfigOC.OCNPolarity = TIM_OCNPOLARITY_HIGH;
  sConfigOC.OCFastMode = TIM_OCFAST_DISABLE;
  sConfigOC.OCIdleState = TIM_OCIDLESTATE_RESET;
  sConfigOC.OCNIdleState = TIM_OCNIDLESTATE_RESET;
  if (HAL_TIM_PWM_ConfigChannel(&htim1, &sConfigOC, TIM_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  sBreakDeadTimeConfig.OffStateRunMode = TIM_OSSR_DISABLE;
  sBreakDeadTimeConfig.OffStateIDLEMode = TIM_OSSI_DISABLE;
  sBreakDeadTimeConfig.LockLevel = TIM_LOCKLEVEL_OFF;
  sBreakDeadTimeConfig.DeadTime = 0;
  sBreakDeadTimeConfig.BreakState = TIM_BREAK_DISABLE;
  sBreakDeadTimeConfig.BreakPolarity = TIM_BREAKPOLARITY_HIGH;
  sBreakDeadTimeConfig.AutomaticOutput = TIM_AUTOMATICOUTPUT_DISABLE;
  if (HAL_TIMEx_ConfigBreakDeadTime(&htim1, &sBreakDeadTimeConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM1_Init 2 */

  /* USER CODE END TIM1_Init 2 */
  HAL_TIM_MspPostInit(&htim1);

}

/**
  * @brief TIM6 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM6_Init(void)
{

  /* USER CODE BEGIN TIM6_Init 0 */

  /* USER CODE END TIM6_Init 0 */

  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM6_Init 1 */

  /* USER CODE END TIM6_Init 1 */
  htim6.Instance = TIM6;
  htim6.Init.Prescaler = 89;
  htim6.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim6.Init.Period = 999;
  htim6.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim6) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_RESET;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim6, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM6_Init 2 */

  /* USER CODE END TIM6_Init 2 */

}

/**
  * @brief USART6 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART6_UART_Init(void)
{

  /* USER CODE BEGIN USART6_Init 0 */

  /* USER CODE END USART6_Init 0 */

  /* USER CODE BEGIN USART6_Init 1 */

  /* USER CODE END USART6_Init 1 */
  huart6.Instance = USART6;
  huart6.Init.BaudRate = 115200;
  huart6.Init.WordLength = UART_WORDLENGTH_8B;
  huart6.Init.StopBits = UART_STOPBITS_1;
  huart6.Init.Parity = UART_PARITY_NONE;
  huart6.Init.Mode = UART_MODE_TX_RX;
  huart6.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart6.Init.OverSampling = UART_OVERSAMPLING_16;
  if (HAL_UART_Init(&huart6) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART6_Init 2 */

  /* USER CODE END USART6_Init 2 */

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOC_CLK_ENABLE();
  __HAL_RCC_GPIOH_CLK_ENABLE();
  __HAL_RCC_GPIOE_CLK_ENABLE();
  __HAL_RCC_GPIOD_CLK_ENABLE();
  __HAL_RCC_GPIOB_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15, GPIO_PIN_RESET);

  /*Configure GPIO pin : PC15 */
  GPIO_InitStruct.Pin = GPIO_PIN_15;
  GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

  /*Configure GPIO pins : PD13 PD14 PD15 */
  GPIO_InitStruct.Pin = GPIO_PIN_13|GPIO_PIN_14|GPIO_PIN_15;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOD, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */

/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */

  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t *file, uint32_t line)
{
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     tex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
