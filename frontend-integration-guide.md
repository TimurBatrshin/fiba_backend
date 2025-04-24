# Руководство по интеграции с API загрузки фотографий

## Обновления в API для загрузки фотографий

В API были внесены изменения для улучшения работы с загрузкой фотографий профиля. Эти изменения включают:

1. Улучшение обработки ошибок и детальное логирование на сервере
2. Добавление нового эндпоинта для загрузки по ID профиля
3. Автоматическое создание директорий для хранения файлов
4. Корректная обработка Content-Type multipart/form-data

## Эндпоинты API для работы с профилями и фотографиями

### 1. Загрузка фотографии профиля (для текущего пользователя)

```
POST /api/profile/photo
Content-Type: multipart/form-data
```

Параметры запроса:
- `photo` (обязательный) - файл изображения
- `tournaments_played` (опциональный) - количество сыгранных турниров
- `total_points` (опциональный) - общее количество очков
- `rating` (опциональный) - рейтинг

Пример запроса:
```javascript
const formData = new FormData();
formData.append('photo', fileInput.files[0]);

fetch('/api/profile/photo', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Ошибка:', error));
```

### 2. Загрузка фотографии профиля по ID

```
POST /api/profile/{id}/photo
Content-Type: multipart/form-data
```

Параметры запроса:
- `photo` (обязательный) - файл изображения

Пример запроса:
```javascript
const profileId = 6; // ID профиля
const formData = new FormData();
formData.append('photo', fileInput.files[0]);

fetch(`/api/profile/${profileId}/photo`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Ошибка:', error));
```

### 3. Получение информации о профиле по ID

```
GET /api/profile/{id}
```

Пример запроса:
```javascript
const profileId = 6; // ID профиля

fetch(`/api/profile/${profileId}`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Ошибка:', error));
```

## Рекомендации по реализации на фронтенде

### 1. Использование axios

```javascript
import axios from 'axios';

// Создайте функцию для загрузки фото
async function uploadProfilePhoto(profileId, photoFile) {
  try {
    const formData = new FormData();
    formData.append('photo', photoFile);
    
    const response = await axios.post(`/api/profile/${profileId}/photo`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    return response.data;
  } catch (error) {
    console.error('Ошибка при загрузке фото профиля:', error);
    // Проверяем наличие деталей ошибки от сервера
    if (error.response && error.response.data) {
      throw new Error(error.response.data.error || 'Ошибка при загрузке фото');
    }
    throw error;
  }
}
```

### 2. Пример компонента React для загрузки фото

```jsx
import React, { useState } from 'react';
import axios from 'axios';

function ProfilePhotoUploader({ profileId, onSuccess }) {
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setFile(e.target.files[0]);
      setError(null);
    }
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Пожалуйста, выберите файл');
      return;
    }
    
    // Проверка файла перед отправкой
    const errorMessage = validateImage(file);
    if (errorMessage) {
      setError(errorMessage);
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const formData = new FormData();
      formData.append('photo', file);
      
      const response = await axios.post(`/api/profile/${profileId}/photo`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      setLoading(false);
      if (onSuccess) {
        onSuccess(response.data);
      }
    } catch (error) {
      setLoading(false);
      let errorMessage = 'Ошибка при загрузке фото';
      
      if (error.response && error.response.data && error.response.data.error) {
        errorMessage = error.response.data.error;
      }
      
      setError(errorMessage);
    }
  };
  
  // Функция валидации изображения
  const validateImage = (file) => {
    // Проверка типа файла
    if (!file.type.match('image.*')) {
      return 'Пожалуйста, выберите изображение';
    }
    
    // Проверка размера файла (не более 5 МБ)
    if (file.size > 5 * 1024 * 1024) {
      return 'Размер файла не должен превышать 5 МБ';
    }
    
    return null; // Нет ошибок
  };
  
  return (
    <div className="photo-uploader">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="photo">Выберите фото профиля:</label>
          <input 
            type="file" 
            id="photo" 
            accept="image/*" 
            onChange={handleFileChange} 
            disabled={loading}
          />
        </div>
        
        {error && <div className="error-message">{error}</div>}
        
        <button type="submit" disabled={!file || loading}>
          {loading ? 'Загрузка...' : 'Загрузить фото'}
        </button>
      </form>
    </div>
  );
}

export default ProfilePhotoUploader;
```

## Обработка ошибок

Сервер теперь возвращает более подробные сообщения об ошибках с кодами HTTP-статусов и детальным описанием проблемы:

```json
{
  "error": "Описание ошибки",
  "status": 500
}
```

Основные коды ошибок:
- 400 - Некорректный запрос (например, отсутствует обязательный параметр)
- 401 - Пользователь не авторизован 
- 403 - Недостаточно прав для выполнения операции
- 404 - Ресурс не найден (профиль не существует)
- 500 - Внутренняя ошибка сервера

## Отображение загруженных изображений

Загруженные изображения доступны по URL, возвращаемому в ответе:

```json
{
  "id": 6,
  "photo_url": "/uploads/profiles/12345678-1234-1234-1234-123456789abc.jpg",
  "message": "Фотография профиля успешно загружена"
}
```

Для отображения изображения используйте относительный путь:

```jsx
<img src={profileData.photo_url} alt="Фото профиля" className="profile-image" />
```

## Возможные проблемы и их решение

### 1. При загрузке фото профиля получаю ошибку 500 (Internal Server Error)

Убедитесь, что:
- В запросе используется правильное имя параметра: `photo` (не `file` или что-то другое)
- Content-Type запроса установлен как `multipart/form-data`
- Файл имеет допустимый формат и размер (не более 10 МБ)
- Сервер имеет права на запись в директорию `uploads/profiles`

### 2. Фото не отображается после успешной загрузки

Проверьте:
- URL изображения возвращается корректно в ответе
- Сервер настроен на раздачу статических файлов из директории `uploads`
- В консоли браузера нет ошибок загрузки изображения 