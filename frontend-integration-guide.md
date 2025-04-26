# Руководство по интеграции фронтенда для работы с аватарками

## Компоненты

### 1. Компонент отображения аватарки (ProfileAvatar)

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

  useEffect(() => {
    const fetchAvatar = async () => {
      try {
        const response = await axios.get(`/api/files/user/${userId}/avatar`);
        setAvatarUrl(response.data.avatar_url);
      } catch (err) {
        setError('Не удалось загрузить аватарку');
        console.error('Ошибка при загрузке аватарки:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAvatar();
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
          onError={() => setAvatarUrl(null)}
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

### 2. Компонент загрузки аватарки (AvatarUploader)

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

      if (onUploadSuccess) {
        onUploadSuccess(response.data.avatar_url);
      }
    } catch (err) {
      const errorMessage = 'Ошибка при загрузке аватарки';
      setError(errorMessage);
      if (onUploadError) {
        onUploadError(errorMessage);
      }
      console.error('Ошибка при загрузке аватарки:', err);
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

### 3. Использование в профиле

```typescript
// pages/Profile.tsx
import React from 'react';
import ProfileAvatar from '../components/Profile/ProfileAvatar';
import AvatarUploader from '../components/Profile/AvatarUploader';

const Profile: React.FC = () => {
  const userId = 123; // ID текущего пользователя

  const handleUploadSuccess = (avatarUrl: string) => {
    // Обновление UI после успешной загрузки
    console.log('Аватарка успешно загружена:', avatarUrl);
  };

  const handleUploadError = (error: string) => {
    // Обработка ошибки
    console.error('Ошибка при загрузке аватарки:', error);
  };

  return (
    <div className="profile-page">
      <div className="profile-header">
        <ProfileAvatar userId={userId} className="large" />
        <AvatarUploader
          userId={userId}
          onUploadSuccess={handleUploadSuccess}
          onUploadError={handleUploadError}
        />
      </div>
      {/* Остальной контент профиля */}
    </div>
  );
};

export default Profile;
```

## Стили

```css
/* styles/Profile.css */
.profile-avatar {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
  color: #666;
  font-size: 14px;
}

.avatar-uploader {
  margin-top: 10px;
}

.upload-button {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.upload-button:hover {
  background-color: #0056b3;
}

.upload-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  margin-top: 8px;
  font-size: 14px;
}
```

## Установка и использование

1. Установите необходимые зависимости:
```bash
npm install axios
```

2. Импортируйте стили в вашем приложении:
```typescript
import './styles/Profile.css';
```

3. Используйте компоненты в нужных местах вашего приложения.

## API Endpoints

### Получение аватарки пользователя
```
GET /api/files/user/{userId}/avatar
```
Ответ:
```json
{
    "avatar_url": "/uploads/avatars/12345678-1234-1234-1234-123456789abc.jpg"
}
```

### Загрузка аватарки пользователя
```
POST /api/files/user/{userId}/avatar
Content-Type: multipart/form-data
```
Параметры:
- `file`: файл изображения

Ответ:
```json
{
    "avatar_url": "/uploads/avatars/12345678-1234-1234-1234-123456789abc.jpg",
    "message": "Аватарка успешно загружена"
}
```

## Ограничения

1. Поддерживаемые форматы изображений:
   - JPEG
   - PNG
   - GIF
   - BMP
   - WEBP

2. Максимальный размер файла: 5MB

3. Рекомендуемые размеры изображения:
   - Минимум: 200x200 пикселей
   - Оптимально: 400x400 пикселей
   - Максимум: 1024x1024 пикселей

## Обработка ошибок

1. Ошибки загрузки файла:
   - Неверный формат файла
   - Превышение размера файла
   - Ошибки сети
   - Ошибки сервера

2. Ошибки отображения:
   - Файл не найден
   - Ошибка загрузки изображения
   - Ошибка сети

## Рекомендации по использованию

1. Всегда проверяйте тип и размер файла перед загрузкой
2. Используйте плейсхолдеры во время загрузки
3. Обрабатывайте все возможные ошибки
4. Предоставляйте пользователю обратную связь о процессе загрузки
5. Используйте кэширование для оптимизации производительности
6. Следите за консистентностью данных между компонентами 