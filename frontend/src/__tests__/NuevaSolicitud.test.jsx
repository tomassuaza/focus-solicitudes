import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import NuevaSolicitud from '../pages/NuevaSolicitud.jsx';

vi.mock('../services/api.js', () => ({
  solicitudesApi: {
    crear: vi.fn(),
  },
}));

import { solicitudesApi } from '../services/api.js';

function renderPage() {
  return render(
    <MemoryRouter>
      <NuevaSolicitud />
    </MemoryRouter>
  );
}

describe('<NuevaSolicitud />', () => {
  beforeEach(() => {
    solicitudesApi.crear.mockReset();
  });

  it('renderiza los campos obligatorios', () => {
    renderPage();
    expect(screen.getByLabelText(/Tipo de trabajo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Prioridad/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Descripcion/i)).toBeInTheDocument();
  });

  it('no envia si la descripcion esta vacia y muestra error', async () => {
    renderPage();
    fireEvent.click(screen.getByRole('button', { name: /Registrar solicitud/i }));
    await waitFor(() => {
      expect(screen.getByText(/descripcion es obligatoria/i)).toBeInTheDocument();
    });
    expect(solicitudesApi.crear).not.toHaveBeenCalled();
  });

  it('envia el payload y muestra confirmacion al exito', async () => {
    solicitudesApi.crear.mockResolvedValueOnce({
      id: 42, estado: 'REGISTRADA', unidad: 'DISENO',
    });
    renderPage();
    fireEvent.change(screen.getByLabelText(/Descripcion/i), {
      target: { value: 'Diseño de banner mensual' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Registrar solicitud/i }));
    await waitFor(() => {
      expect(solicitudesApi.crear).toHaveBeenCalledTimes(1);
      expect(screen.getByText(/Solicitud #42 creada/)).toBeInTheDocument();
    });
  });

  it('muestra el error que devuelve la API', async () => {
    solicitudesApi.crear.mockRejectedValueOnce(new Error('Cliente no encontrado: 99'));
    renderPage();
    fireEvent.change(screen.getByLabelText(/Descripcion/i), {
      target: { value: 'algo' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Registrar solicitud/i }));
    await waitFor(() => {
      expect(screen.getByText(/Cliente no encontrado/i)).toBeInTheDocument();
    });
  });
});
