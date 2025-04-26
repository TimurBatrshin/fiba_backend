# Руководство по интеграции аватарок: Бэкенд и Фронтенд

## Процесс обмена данными

### 1. Получение аватарки (GET запрос)

**Бэкенд отправляет:**
```json
{
    "avatar_url": "/uploads/avatars/12345678-1234-1234-1234-123456789abc.jpg"
}
```

**Фронтенд обрабатывает:**
- Получает URL аватарки из ответа
- Использует URL в теге `<img>`
- Браузер автоматически загружает изображение по этому URL

### 2. Загрузка аватарки (POST запрос)

**Фронтенд отправляет:**
- FormData с файлом изображения
- Content-Type: multipart/form-data

**Бэкенд отвечает:**
```json
{
    "avatar_url": "/uploads/avatars/12345678-1234-1234-1234-123456789abc.jpg",
    "message": "Аватарка успешно загружена"
}
```

**Фронтенд обрабатывает:**
- Получает новый URL аватарки
- Обновляет состояние компонента
- Обновляет отображение

## Компоненты фронтенда

### 1. Компонент отображения аватарки

```typescript
// components/Profile/ProfileAvatar.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

interface ProfileAvatarProps {
  userId: number;
  className?: string;
}

const ProfileAvatar: React.FC<ProfileAvatarProps> = ({ userId, className }) => {
  const [avatarUrl, setAvatarUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Функция для получения URL аватарки
  const fetchAvatarUrl = async () => {
    try {
      const response = await axios.get(`/api/files/user/${userId}/avatar`);
      // Получаем только URL аватарки
      const { avatar_url } = response.data;
      setAvatarUrl(avatar_url);
    } catch (err) {
      setError('Не удалось загрузить аватарку');
      console.error('Ошибка при загрузке аватарки:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAvatarUrl();
  }, [userId]);

  if (loading) {
    return <div className="avatar-placeholder">Загрузка...</div>;
  }

  if (error) {
    return <div className="avatar-placeholder">Ошибка загрузки</div>;
  }

  return (
    <div className={`profile-avatar ${className || ''}`}>
      {avatarUrl ? (
        <img 
          src={avatarUrl} 
          alt="Аватар пользователя" 
          className="avatar-image"
          onError={() => {
            // Если изображение не загрузилось, показываем плейсхолдер
            setAvatarUrl(null);
          }}
        />
      ) : (
        <div className="avatar-placeholder">
          <span>Нет фото</span>
        </div>
      )}
    </div>
  );
};

export default ProfileAvatar;
```

### 2. Компонент загрузки аватарки

```typescript
// components/Profile/AvatarUploader.tsx
import React, { useState, useRef } from 'react';
import axios from 'axios';

interface AvatarUploaderProps {
  userId: number;
  onUploadSuccess?: (avatarUrl: string) => void;
  onUploadError?: (error: string) => void;
}

const AvatarUploader: React.FC<AvatarUploaderProps> = ({
  userId,
  onUploadSuccess,
  onUploadError
}) => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Проверка типа файла
    if (!file.type.startsWith('image/')) {
      setError('Пожалуйста, выберите изображение');
      return;
    }

    // Проверка размера файла (5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('Размер файла не должен превышать 5MB');
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await axios.post(
        `/api/files/user/${userId}/avatar`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      );

      // Получаем только URL новой аватарки
      const { avatar_url } = response.data;
      
      if (onUploadSuccess) {
        onUploadSuccess(avatar_url);
      }
    } catch (err) {
      const errorMessage = 'Ошибка при загрузке аватарки';
      setError(errorMessage);
      if (onUploadError) {
        onUploadError(errorMessage);
      }
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="avatar-uploader">
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileChange}
        accept="image/*"
        style={{ display: 'none' }}
      />
      
      <button
        onClick={() => fileInputRef.current?.click()}
        disabled={uploading}
        className="upload-button"
      >
        {uploading ? 'Загрузка...' : 'Изменить фото'}
      </button>

      {error && <div className="error-message">{error}</div>}
    </div>
  );
};

export default AvatarUploader;
```

## Особенности реализации

1. **Бэкенд отправляет только URL'ы:**
   - Не отправляет сами файлы в JSON
   - Уменьшает нагрузку на сервер
   - Уменьшает трафик

2. **Фронтенд работает только с URL'ами:**
   - Не обрабатывает сами файлы
   - Использует браузер для загрузки изображений
   - Проще в реализации

3. **Браузер автоматически:**
   - Загружает изображения по URL'ам
   - Кэширует изображения
   - Обрабатывает ошибки загрузки

4. **Обработка ошибок:**
   - Проверка типа файла
   - Проверка размера файла
   - Обработка ошибок сети
   - Обработка ошибок загрузки изображения

## Рекомендации

1. **Для бэкенда:**
   - Всегда возвращать только URL'ы
   - Проверять тип и размер файлов
   - Обрабатывать ошибки загрузки
   - Логировать все операции

2. **Для фронтенда:**
   - Использовать плейсхолдеры
   - Обрабатывать ошибки загрузки
   - Обновлять UI после успешной загрузки
   - Кэшировать URL'ы

3. **Для оптимизации:**
   - Использовать кэширование браузера
   - Оптимизировать размеры изображений
   - Использовать lazy loading
   - Предзагружать важные изображения 