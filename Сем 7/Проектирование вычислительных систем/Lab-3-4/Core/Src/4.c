/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main2.c
  * @brief          : Музыкальная клавиатура с матричной клавиатурой I2C
  *                   Лабораторная работа 4
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

/* Private define ------------------------------------------------------------*/
#define KB_I2C_ADDRESS (0xE2)
#define KB_I2C_READ_ADDRESS ((KB_I2C_ADDRESS) | 1)
#define KB_I2C_WRITE_ADDRESS ((KB_I2C_ADDRESS) & ~1)
#define KB_INPUT_REG (0x0)
#define KB_OUTPUT_REG (0x1)
#define KB_CONFIG_REG (0x3)
#define KB_KEY_DEBOUNCE_TIME (200)

// Частоты нот первой октавы (индекс 4)
#define NOTE_C_BASE  261.63f  // До
#define NOTE_D_BASE  293.67f  // Ре
#define NOTE_E_BASE  329.63f  // Ми
#define NOTE_F_BASE  349.23f  // Фа
#define NOTE_G_BASE  392.00f  // Соль
#define NOTE_A_BASE  440.00f  // Ля
#define NOTE_B_BASE  493.88f  // Си

// Параметры октав и длительности
#define MIN_OCTAVE 0          // Субконтроктава
#define MAX_OCTAVE 8          // Пятая октава
#define DEFAULT_OCTAVE 4      // Первая октава
#define MIN_DURATION 1        // 0.1 с
#define MAX_DURATION 50       // 5.0 с
#define DEFAULT_DURATION 10   // 1.0 с

// Частота таймера TIM1 (APB2)
#define TIM1_FREQ 90000000

/* External function prototypes ----------------------------------------------*/
void SystemClock_Config(void);
void MX_GPIO_Init(void);
void MX_TIM1_Init(void);
void MX_TIM6_Init(void);
void MX_USART6_UART_Init(void);
void MX_I2C1_Init(void);
void Error_Handler(void);

/* Private variables ---------------------------------------------------------*/
I2C_HandleTypeDef hi2c1;
TIM_HandleTypeDef htim1;
TIM_HandleTypeDef htim6;
UART_HandleTypeDef huart6;

// ----- Глобальные переменные музыкальной клавиатуры -----
static uint8_t current_octave = DEFAULT_OCTAVE;   // Текущая октава (4 = первая октава)
static uint8_t note_duration = DEFAULT_DURATION;  // Длительность ноты в единицах по 0.1с
static volatile bool is_playing = false;          // Флаг воспроизведения
static volatile uint32_t play_counter = 0;        // Счетчик для длительности в миллисекундах
static volatile uint32_t tim6_counter = 0;        // Счетчик миллисекунд для TIM6
static volatile bool play_all_notes_requested = false; // Флаг запроса воспроизведения всех нот

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

// ----- Переменные клавиатуры -----
static bool is_test_keyboard_mode = false;
static uint32_t last_pressing_time = 0;
static int last_pressed_btn_index = -1;
static bool last_btn_state = false;

// Раскладка клавиатуры для музыкального режима
// Кнопки 1-12:
//  1  2  3  <- До, Ре, Ми
//  4  5  6  <- Фа, Соль, Ля
//  7  8  9  <- Си, Октава+, Октава-
// 10 11 12  <- Длит+, Длит-, Enter
static const char keyboard_layout[12] = {
    '1', '2', '3',  // До, Ре, Ми
    '4', '5', '6',  // Фа, Соль, Ля
    '7', '+', '-',  // Си, Октава+, Октава-
    'A', 'a', '\r'  // Длит+, Длит-, Enter
};

/* Private function prototypes -----------------------------------------------*/
static void uart_send_string(const char* str);
static float get_note_frequency(uint8_t note_index, uint8_t octave);
static void play_note(uint8_t note_index, uint8_t octave, uint8_t duration_units);
static void stop_note(void);
static int get_pressed_btn_index(void);
static bool is_btn_press(void);
static void set_green_led(bool on);
static void set_yellow_led(bool on);
static void set_red_led(bool on);

/* Function implementations --------------------------------------------------*/

// Отправка строки через UART
static void uart_send_string(const char* str) {
    HAL_UART_Transmit(&huart6, (uint8_t*)str, strlen(str), 1000);
}

// Вычисление частоты ноты для заданной октавы
static float get_note_frequency(uint8_t note_index, uint8_t octave) {
    float base_freq = base_frequencies[note_index];
    int octave_shift = (int)octave - 4;  // Сдвиг относительно первой октавы
    float frequency = base_freq * powf(2.0f, (float)octave_shift);
    return frequency;
}

// Воспроизведение ноты
static void play_note(uint8_t note_index, uint8_t octave, uint8_t duration_units) {
    if (note_index >= 7) return;

    float frequency = get_note_frequency(note_index, octave);
    uint32_t freq_int = (uint32_t)frequency;

    // Настройка ARR для нужной частоты
    htim1.Instance->ARR = TIM1_FREQ / (freq_int * htim1.Instance->PSC) - 1;

    // Установка скважности 50%
    htim1.Instance->CCR1 = htim1.Instance->ARR >> 1;

    // Установка длительности воспроизведения
    play_counter = duration_units * 100;  // Перевод в миллисекунды
    is_playing = true;
}

// Остановка воспроизведения ноты
static void stop_note(void) {
    htim1.Instance->CCR1 = 0;  // Выключаем звук
    is_playing = false;
}

// Управление светодиодами
static void set_green_led(bool on) {
    HAL_GPIO_WritePin(GPIOD, GPIO_PIN_13, on ? GPIO_PIN_SET : GPIO_PIN_RESET);
}

static void set_yellow_led(bool on) {
    HAL_GPIO_WritePin(GPIOD, GPIO_PIN_14, on ? GPIO_PIN_SET : GPIO_PIN_RESET);
}

static void set_red_led(bool on) {
    HAL_GPIO_WritePin(GPIOD, GPIO_PIN_15, on ? GPIO_PIN_SET : GPIO_PIN_RESET);
}

// Проверка нажатия кнопки на боковой панели
static bool is_btn_press() {
    return HAL_GPIO_ReadPin(GPIOC, GPIO_PIN_15) == 0;
}

// Опрос матричной клавиатуры через I2C
static int get_pressed_btn_index() {
    const uint32_t t = HAL_GetTick();
    if (t - last_pressing_time < KB_KEY_DEBOUNCE_TIME) return -1;

    int index = -1;
    uint8_t reg_buffer = ~0;
    uint8_t tmp = 0;
    int pressed_count = 0;
    int stroka[4] = {0, 0, 0, 0};
    int stolbec[3] = {0, 0, 0};

    HAL_I2C_Mem_Write(&hi2c1, KB_I2C_WRITE_ADDRESS, KB_OUTPUT_REG, 1, &tmp, 1, KB_KEY_DEBOUNCE_TIME);

    for (int row = 0; row < 4; row++) {
        uint8_t buf = ~((uint8_t)(1 << row));
        HAL_I2C_Mem_Write(&hi2c1, KB_I2C_WRITE_ADDRESS, KB_CONFIG_REG, 1, &buf, 1, KB_KEY_DEBOUNCE_TIME);
        HAL_Delay(10);
        HAL_I2C_Mem_Read(&hi2c1, KB_I2C_READ_ADDRESS, KB_INPUT_REG, 1, &reg_buffer, 1, KB_KEY_DEBOUNCE_TIME);

        switch(reg_buffer >> 4) {
            case 6:
                index = (pressed_count == 0) ? row * 3 + 1 : -1;
                pressed_count++;
                stroka[row]++;
                stolbec[0]++;
                break;
            case 5:
                index = (pressed_count == 0) ? row * 3 + 2 : -1;
                pressed_count++;
                stroka[row]++;
                stolbec[1]++;
                break;
            case 3:
                index = (pressed_count == 0) ? row * 3 + 3 : -1;
                pressed_count++;
                stroka[row]++;
                stolbec[2]++;
                break;
            default:
                break;
        }
    }

    if (index != -1) last_pressing_time = t;

    // Защита от переповторов
    if (index == last_pressed_btn_index) {
        return -1;
    }

    last_pressed_btn_index = index;

    // Проверка множественного нажатия
    int sum_stroka = 0;
    int sum_stolbec = 0;

    for (int j = 0; j < 4; j++) {
        sum_stroka += stroka[j];
    }

    for (int j = 0; j < 3; j++) {
        sum_stolbec += stolbec[j];
    }

    // Если нажато больше одной кнопки - возвращаем -1
    if (sum_stroka != 1 || sum_stolbec != 1) {
        return -1;
    }

    return index;
}

// Преобразование индекса кнопки в символ
static char key2char(int key) {
    if (key < 1 || key > 12) return 0;
    return keyboard_layout[key - 1];
}

// Обработка команды воспроизведения ноты
static void handle_note_command(char note_char) {
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

// Обработка команды изменения октавы
static void handle_octave_change(char direction) {
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

// Обработка команды изменения длительности
static void handle_duration_change(char command) {
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

// Запрос воспроизведения всех нот октавы
static void request_play_all_notes(void) {
    play_all_notes_requested = true;

    char msg[128];
    sprintf(msg, "\r\nВоспроизведение всех нот октавы: %s, длительность: %.1f с\r\n",
            octave_names[current_octave],
            (float)note_duration / 10.0f);
    uart_send_string(msg);
}

// Callback таймера TIM6 (вызывается каждую 1 мс)
void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef *htim) {
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

/* Main function -------------------------------------------------------------*/
int main(void)
{
    /* MCU Configuration--------------------------------------------------------*/

    /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
    HAL_Init();

    /* Configure the system clock */
    SystemClock_Config();

    /* Initialize all configured peripherals */
    MX_GPIO_Init();
    MX_TIM1_Init();
    MX_TIM6_Init();
    MX_USART6_UART_Init();
    MX_I2C1_Init();

    /* USER CODE BEGIN 2 */

    // Запуск ШИМ для звука
    HAL_TIM_PWM_Start(&htim1, TIM_CHANNEL_1);

    // Запуск таймера TIM6 с прерыванием (1 мс)
    HAL_TIM_Base_Start_IT(&htim6);

    // Приветственное сообщение
    uart_send_string("\r\n");
    uart_send_string("===============================================\r\n");
    uart_send_string("  Музыкальная клавиатура STM32\r\n");
    uart_send_string("  Лабораторная работа 4: I2C и матричная клавиатура\r\n");
    uart_send_string("===============================================\r\n");
    uart_send_string("\r\n");
    uart_send_string("Раскладка клавиатуры:\r\n");
    uart_send_string("  1 | 2  | 3  <- До, Ре, Ми\r\n");
    uart_send_string("  4 | 5  | 6  <- Фа, Соль, Ля\r\n");
    uart_send_string("  7 | 8  | 9  <- Си, Октава+, Октава-\r\n");
    uart_send_string(" 10 | 11 | 12    <- Длит+, Длит-, Enter\r\n");
    uart_send_string("\r\n");
    uart_send_string("Нажмите кнопку на боковой панели для переключения режимов:\r\n");
    uart_send_string("  - Тестовый режим (вывод кодов кнопок)\r\n");
    uart_send_string("  - Прикладной режим (музыкальная клавиатура)\r\n");
    uart_send_string("\r\n");

    char msg[128];
    sprintf(msg, "Текущая октава: %s\r\n", octave_names[current_octave]);
    uart_send_string(msg);
    sprintf(msg, "Текущая длительность: %.1f с\r\n", (float)note_duration / 10.0f);
    uart_send_string(msg);
    uart_send_string("\r\n");
    uart_send_string("Режим: Прикладной (музыкальная клавиатура)\r\n\r\n");

    set_green_led(false);
    set_yellow_led(false);
    set_red_led(false);

    /* USER CODE END 2 */

    /* Infinite loop */
    while (1)
    {
        // Обработка переключения режимов по кнопке на боковой панели
        bool btn_state = is_btn_press();
        if (last_btn_state && !btn_state) {
            is_test_keyboard_mode = !is_test_keyboard_mode;
            if (is_test_keyboard_mode) {
                uart_send_string("\r\n>>> Режим: Тестовый (вывод кодов кнопок)\r\n\r\n");
                set_yellow_led(true);
            } else {
                uart_send_string("\r\n>>> Режим: Прикладной (музыкальная клавиатура)\r\n\r\n");
                set_yellow_led(false);
            }
        }
        last_btn_state = btn_state;

        // Опрос клавиатуры
        int btn_index = get_pressed_btn_index();
        if (btn_index != -1) {
            char received_char = key2char(btn_index);

            if (is_test_keyboard_mode) {
                // Тестовый режим - вывод кода кнопки
                char test_msg[64];
                sprintf(test_msg, "Кнопка %d нажата (символ: '%c', ASCII: %d)\r\n",
                        btn_index, received_char, (int)received_char);
                uart_send_string(test_msg);
            } else {
                // Прикладной режим - обработка команд музыкальной клавиатуры
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
                else if (received_char == '\r') {
                    // Воспроизведение всех нот октавы
                    request_play_all_notes();
                }
            }
        }

        // Обработка запроса воспроизведения всех нот октавы
        if (play_all_notes_requested && !is_playing) {
            play_all_notes_requested = false;

            static uint8_t current_note_index = 0;
            static bool sequence_started = false;

            if (!sequence_started) {
                current_note_index = 0;
                sequence_started = true;
            }

            if (current_note_index < 7) {
                char msg[128];
                sprintf(msg, "Нота: %s\r\n", note_names[current_note_index]);
                uart_send_string(msg);

                play_note(current_note_index, current_octave, note_duration);
                current_note_index++;

                if (current_note_index < 7) {
                    play_all_notes_requested = true;
                } else {
                    uart_send_string("\r\nВоспроизведение завершено\r\n");
                    sequence_started = false;
                    current_note_index = 0;
                }
            }
        }
    }
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
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
void MX_I2C1_Init(void)
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
void MX_TIM1_Init(void)
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
void MX_TIM6_Init(void)
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
void MX_USART6_UART_Init(void)
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
void MX_GPIO_Init(void)
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
