// ========== src/pages/auth/RegisterPage.jsx ==========
/**
 * Página de Registro
 */

import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useForm } from 'react-hook-form';
import { Mail, Lock, User, Phone } from 'lucide-react';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Card from '../../components/common/Card';

const RegisterPage = () => {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const password = watch('password');

  const onSubmit = async (data) => {
    try {
      setLoading(true);
      await registerUser({
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        password: data.password,
        phoneNumber: data.phoneNumber,
      });
      navigate('/dashboard');
    } catch (error) {
      console.error('Error en registro:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Crear Cuenta
          </h1>
          <p className="text-gray-600">
            Regístrate para empezar a gestionar tus proyectos
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Nombre */}
          <Input
            label="Nombre"
            type="text"
            icon={User}
            placeholder="Juan"
            error={errors.firstName?.message}
            {...register('firstName', {
              required: 'El nombre es requerido',
            })}
          />

          {/* Apellido */}
          <Input
            label="Apellido"
            type="text"
            icon={User}
            placeholder="Pérez"
            error={errors.lastName?.message}
            {...register('lastName', {
              required: 'El apellido es requerido',
            })}
          />

          {/* Email */}
          <Input
            label="Correo Electrónico"
            type="email"
            icon={Mail}
            placeholder="tu@email.com"
            error={errors.email?.message}
            {...register('email', {
              required: 'El correo es requerido',
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: 'Correo inválido',
              },
            })}
          />

          {/* Teléfono (opcional) */}
          <Input
            label="Teléfono"
            type="tel"
            icon={Phone}
            placeholder="+51999999999"
            {...register('phoneNumber')}
          />

          {/* Password */}
          <Input
            label="Contraseña"
            type="password"
            icon={Lock}
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password', {
              required: 'La contraseña es requerida',
              minLength: {
                value: 8,
                message: 'Mínimo 8 caracteres',
              },
            })}
          />

          {/* Confirmar Password */}
          <Input
            label="Confirmar Contraseña"
            type="password"
            icon={Lock}
            placeholder="••••••••"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword', {
              required: 'Confirma tu contraseña',
              validate: (value) =>
                value === password || 'Las contraseñas no coinciden',
            })}
          />

          {/* Submit Button */}
          <Button type="submit" fullWidth loading={loading}>
            Crear Cuenta
          </Button>
        </form>

        {/* Login Link */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            ¿Ya tienes cuenta?{' '}
            <Link
              to="/login"
              className="font-medium text-primary-600 hover:text-primary-500"
            >
              Inicia sesión
            </Link>
          </p>
        </div>
      </Card>
    </div>
  );
};

export default RegisterPage;