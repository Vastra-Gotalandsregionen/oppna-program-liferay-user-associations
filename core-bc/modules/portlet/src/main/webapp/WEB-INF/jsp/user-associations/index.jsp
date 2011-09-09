<%--

    Copyright 2010 Västra Götalandsregionen

      This library is free software; you can redistribute it and/or modify
      it under the terms of version 2.1 of the GNU Lesser General Public
      License as published by the Free Software Foundation.

      This library is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU Lesser General Public License for more details.

      You should have received a copy of the GNU Lesser General Public
      License along with this library; if not, write to the
      Free Software Foundation, Inc., 59 Temple Place, Suite 330,
      Boston, MA 02111-1307  USA


--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div style="float: left;">
    <form method="GET">
        <input type="hidden" value="edit">
        <select>
            <option>Externa användare</option>
        </select>
        <input type="submit" value="Välj"/>
    </form>
</div>
<div style="float: left;">
    <input type="text">
</div>
<div style="float: left;">
    <select size="10">
        <option>Extern</option>
        <option>Annan grupp</option>
    </select>
</div>
<div style="float: left;">
    <select size="10">
        <option>Grupp1</option>
        <option>Grupp2</option>
    </select>
</div>